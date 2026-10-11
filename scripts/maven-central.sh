#!/usr/bin/env bash
# Maven Central release steps, run by the release workflow for a vMAJOR.MINOR.PATCH tag.
#
#   scripts/maven-central.sh bundle   Builds, signs and bundles the release into target/central-bundle.zip
#   scripts/maven-central.sh publish  bundle, then uploads to the Central Portal and waits for PUBLISHED
#   scripts/maven-central.sh verify   Waits for the version on Maven Central and resolves it from there
#
# Environment: RELEASE_VERSION (defaults to the POM version), MAVEN_SIGNING_KEY or MAVEN_SIGNING_KEY_FILE,
# MAVEN_SIGNING_PASSWORD, and for publish CENTRAL_USERNAME and CENTRAL_PASSWORD (a Central Portal user token).
# MVN overrides the Maven command (default ./mvnw).
set -euo pipefail

GROUP_PATH="com/assinafy"
ARTIFACT_ID="webforms-java-client-sdk"
MAVEN_PROFILES="release,central"

cd "$(dirname "$0")/.."
mvn_cmd=(${MVN:-./mvnw} --batch-mode --no-transfer-progress)

pom_version="$("${mvn_cmd[@]}" -q -DforceStdout org.apache.maven.plugins:maven-help-plugin:3.5.2:evaluate -Dexpression=project.version)"
version="${RELEASE_VERSION:-$pom_version}"
if [ "$version" != "$pom_version" ]; then
  echo "Release version ${version} does not match the POM version ${pom_version}" >&2
  exit 1
fi
published_pom="https://repo.maven.apache.org/maven2/${GROUP_PATH}/${ARTIFACT_ID}/${version}/${ARTIFACT_ID}-${version}.pom"

bundle() {
  : "${MAVEN_SIGNING_PASSWORD:?Set MAVEN_SIGNING_PASSWORD}"
  local key_args=()
  if [ -n "${MAVEN_SIGNING_KEY_FILE:-}" ]; then
    key_args=("-Dgpg.keyFilePath=${MAVEN_SIGNING_KEY_FILE}")
  elif [ -z "${MAVEN_SIGNING_KEY:-}" ]; then
    echo "Set MAVEN_SIGNING_KEY or MAVEN_SIGNING_KEY_FILE" >&2
    exit 1
  fi

  local repository="$PWD/target/central-repository"
  "${mvn_cmd[@]}" -P"$MAVEN_PROFILES" "${key_args[@]}" \
    -DaltDeploymentRepository="central-bundle::file://${repository}" \
    clean deploy
  find "$repository" -name 'maven-metadata.xml*' -delete
  jar --create --no-manifest --file target/central-bundle.zip -C "$repository" .
  echo "Bundle: target/central-bundle.zip"
}

publish() {
  if curl --fail --silent --output /dev/null "$published_pom"; then
    echo "Maven Central already contains ${version}; skipping the immutable upload"
    return
  fi
  : "${CENTRAL_USERNAME:?Set CENTRAL_USERNAME}"
  : "${CENTRAL_PASSWORD:?Set CENTRAL_PASSWORD}"
  bundle

  local bearer deployment_id status state
  bearer="$(printf '%s:%s' "$CENTRAL_USERNAME" "$CENTRAL_PASSWORD" | base64 | tr -d '\n')"
  deployment_id="$(
    curl --fail --silent --show-error \
      --header "Authorization: Bearer ${bearer}" \
      --form "bundle=@target/central-bundle.zip;type=application/octet-stream" \
      "https://central.sonatype.com/api/v1/publisher/upload?name=${ARTIFACT_ID}-${version}&publishingType=AUTOMATIC"
  )"
  deployment_id="$(printf '%s' "$deployment_id" | tr -d '\r\n')"
  test -n "$deployment_id"

  for attempt in $(seq 1 80); do
    status="$(
      curl --fail --silent --show-error --request POST \
        --header "Authorization: Bearer ${bearer}" \
        "https://central.sonatype.com/api/v1/publisher/status?id=${deployment_id}"
    )"
    state="$(printf '%s' "$status" | jq -r '.deploymentState')"
    case "$state" in
      PUBLISHED) echo "Published ${ARTIFACT_ID} ${version}"; return ;;
      FAILED) printf '%s\n' "$status" | jq . >&2; exit 1 ;;
      PENDING|VALIDATING|VALIDATED|PUBLISHING) ;;
      *) echo "Unexpected Maven Central deployment state: ${state}" >&2; printf '%s\n' "$status" | jq . >&2; exit 1 ;;
    esac
    if [ "$attempt" -eq 80 ]; then
      echo "Maven Central deployment did not finish; deployment ID: ${deployment_id}" >&2
      exit 1
    fi
    sleep 15
  done
}

verify() {
  for attempt in $(seq 1 20); do
    if curl --fail --silent --output /dev/null "$published_pom"; then
      break
    fi
    if [ "$attempt" -eq 20 ]; then
      echo "Published artifact did not reach Maven Central: ${published_pom}" >&2
      exit 1
    fi
    sleep 15
  done
  local repository
  repository="$(mktemp -d)"
  "${mvn_cmd[@]}" -Dmaven.repo.local="$repository" \
    org.apache.maven.plugins:maven-dependency-plugin:3.8.1:get \
    -Dartifact="com.assinafy:${ARTIFACT_ID}:${version}" -Dtransitive=true
  rm -rf "$repository"
}

case "${1:-}" in
  bundle) bundle ;;
  publish) publish ;;
  verify) verify ;;
  *) echo "Usage: $0 bundle|publish|verify" >&2; exit 2 ;;
esac
