# Installation

## Requirements

- Java 25 or later (the artifact is compiled to Java 25 bytecode)
- Maven 3.9.16 or newer 3.x (the wrapper pins 3.9.16); use a Gradle release that supports JDK 25

The artifact runs on Java 25 and every later compatible JDK. The included wrapper pins Maven 3.9.16, and CI
compiles, tests, and packages the release artifacts on the current JDK 25 LTS.

## Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.2.0</version>
</dependency>
```

Then run:

```bash
mvn verify
```

For an application consuming the released package, `mvn verify` or `mvn package` resolves the dependency as
part of the normal build. For SDK development, prefer the checked-in Maven Wrapper so every environment uses
the repository-pinned Maven release: `./mvnw verify`.

## Gradle

```groovy
dependencies {
    implementation 'com.assinafy:webforms-java-client-sdk:2.2.0'
}
```

Or with Kotlin DSL:

```kotlin
dependencies {
    implementation("com.assinafy:webforms-java-client-sdk:2.2.0")
}
```

## GitHub Packages

To install from GitHub Packages, add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/assinafy/webforms-java-client</url>
    </repository>
</repositories>
```

And configure authentication in `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>${env.GITHUB_TOKEN}</password>
    </server>
</servers>
```

For local installs, `GITHUB_TOKEN` must be a GitHub personal access token (classic) with `read:packages` and
access to the repository. `GITHUB_TOKEN` supplied to GitHub Actions is repository-scoped; do not put a token in
`pom.xml` or commit `settings.xml`.

For Gradle, declare the same authenticated repository:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/assinafy/webforms-java-client")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

Set `GITHUB_USERNAME` for local Gradle builds; GitHub Actions supplies `GITHUB_ACTOR`. In both environments,
`GITHUB_TOKEN` must have access to the package.

## Environment Variables

Set these variables in your shell or deployment secret manager:

```bash
export ASSINAFY_API_KEY=k_your_api_key
export ASSINAFY_ACCOUNT_ID=your_account_id
```

The SDK does not load `.env` files itself; pass values explicitly through `AssinafyClientOptions`. Do not
commit local environment or Maven settings files containing credentials. In CI, store both variables as
masked secrets and pass them only to jobs that need sandbox access. Production credentials must never be used
by test jobs.

```java
AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID")));
```

## Transitive Dependencies

The SDK pulls in:

| Dependency | Version | Purpose |
|---|---:|---|
| `com.squareup.okhttp3:okhttp-jvm` | 5.5.0 | HTTP client |
| `com.fasterxml.jackson.core:jackson-databind` | 2.22.2 | JSON data binding |
| `com.fasterxml.jackson.core:jackson-core` | 2.22.2 | JSON streaming |
| `com.fasterxml.jackson.core:jackson-annotations` | 2.22 | JSON model annotations |
| `com.squareup.okio:okio-jvm` | 3.18.1 | OkHttp I/O runtime (transitive) |
