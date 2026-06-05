# Installation

## Requirements

- Java 21 or later (the artifact is compiled to Java 21 bytecode)
- Maven 3.8+ or Gradle 7+ (a Maven Wrapper, `./mvnw`, is included for contributors)

## Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>1.5.0</version>
</dependency>
```

Then run:

```bash
mvn install
```

## Gradle

```groovy
dependencies {
    implementation 'com.assinafy:webforms-java-client-sdk:1.5.0'
}
```

Or with Kotlin DSL:

```kotlin
dependencies {
    implementation("com.assinafy:webforms-java-client-sdk:1.5.0")
}
```

## GitHub Packages

To install from GitHub Packages, add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/assinafy/webforms-java-client-sdk</url>
    </repository>
</repositories>
```

And configure authentication in `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>${GITHUB_TOKEN}</password>
    </server>
</servers>
```

## Environment Variables

Create a `.env` file or set these variables in your environment:

```bash
export ASSINAFY_API_KEY=k_your_api_key
export ASSINAFY_ACCOUNT_ID=your_account_id
```

## Transitive Dependencies

The SDK pulls in:

| Dependency                       | Version   | Purpose              |
|----------------------------------|-----------|----------------------|
| `com.squareup.okhttp3:okhttp`    | 4.12.0    | HTTP client          |
| `com.fasterxml.jackson.core:jackson-databind` | 2.18.2 | JSON serialization |
