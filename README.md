# java-config-fallback

[![Build Status](https://travis-ci.org/unruly/java-config-fallback.svg?branch=master)](https://travis-ci.org/unruly/java-config-fallback)
[![Release Version](https://img.shields.io/maven-central/v/co.unruly/java-config-fallback.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22co.unruly%22%20AND%20a%3A%22java-config-fallback%22)

A lightweight library to query a hierarchy of configuration sources

```java
Configuration config = Configuration.of(
    secretsManager("my-secret-name", "eu-west-1"), // { "username": "user" }
    properties("/etc/my-app-config.properties"),   // { "username": "anotherUser", "password": "secret" }
);

Optional<String> username = config.get("username"); // Optional[user]
Optional<String> password = config.get("password"); // Optional[secret] 
```

## Contents

- [Install](#install)
- [Usage](#usage)
  - [Fallback Behaviour](#fallback-behaviour)
- [Supported Configuration Sources](#supported-configuration-sources)
  - [Map](#map)
  - [Properties](#properties)
  - [System Properties](#system-properties)
  - [Environment](#environment)
  - [AWS Secrets Manager](#aws-secrets-manager)
- [Extending with custom Configuration Sources](#extending-with-custom-configuration-sources)
- [Contributing](#contributing)
  - [Code of Conduct](#code-of-conduct)
  - [Getting Started](#getting-started)
  - [Releasing a Change](#releasing-a-change)
- [License](#license)
- [Design Decisions](#design-decisions)

## Install

This library is available on Maven Central

```xml
<dependency>
    <groupId>co.unruly</groupId>
    <artifactId>java-config-fallback</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Fallback Behaviour

When queried for a key, a `Configuration` instance will query each `ConfigurationSource` passed to its builder **in order** until it finds a key.

Example:

```java
Configuration config = Configuration.of(
    properties("high-priority-config.properties"),
    properties("default.properties"),
    environment()
);
```

* This configuration will first look for a key in `high-priority-config.properties`.
* If it is not there, it will then look for that key in `default.properties`
* If it is not in either of those, then it will check the application's environment variables.

Behaviour if a key cannot be retrieved from any of the provided sources (whether from missing key or missing file or network timeout) depends on the method used to query.

| Method       | Parameters                   | Success Behaviour                      | Failure Behaviour                              |
|:-------------|:-----------------------------|:---------------------------------------|:-----------------------------------------------|
| `.get()`     | `String key`                 | Returns `Optional<String>.of(<value>)` | Returns `Optional<String>.empty()`             |
| `.get()`     | `String key, String default` | Returns `Optional<String>.of(<value>)` | Returns `Optional<String>.of(<default-value>)` |
| `.require()` | `String key`                 | Returns `<value>` as a String          | Throws `ConfigurationMissing` exception        |

## Supported Configuration Sources

### Map

The `.map()` configuration source takes a `Map<String, String>` as a datasource.

```java
Map<String, String> credentials = new HashMap<>();
credentials.put("username","foo");
credentials.put("password","this-is-a-secret!");

Configuration config = Configuration.of(map(credentials));

Optional<String> username = config.get("username"); // Optional[foo]
Optional<String> password = config.get("password"); // Optional[this-is-a-secret!]
```

### Properties

The `.properties()` configuration source takes a `String filePath` pointing to a `.properties` file as a datasource.

```java
Configuration config = Configuration.of(
    properties("/etc/my-app/config.properties"),  // password=new-password
);

Optional<String> password = config.get("password"); // Optional[new-password]
```

### System Properties

The `.systemProperties()` configuration source uses `System.getProperty()` as a datasource.

```java
Configuration config = Configuration.of(
    systemProperties()
);

// Running code with `mvn clean test -Dmy.property=hello-world
Optional<String> myProperty = config.get("my.property"); // Optional[hello-world]

// Default system properties in every JVM
Optional<String> javaVersion = config.get("java.version"); // Optional[1.8.0_12345]
```

### Environment

The `.environment()` configuration source uses environment variables set for this application.

**NOTE**: This assumes environment variables are UPPER_CASE and converts queried keys to UPPER_CASE before lookup.

```java
// With VARIABLE=foo set

Configuration config = Configuration.of(environment());

Optional<String> password = config.get("VARIABLE"); // Optional[foo]
Optional<String> password = config.get("variable"); // Same as above
```

### AWS Secrets Manager

The `.secretsManager()` configuration source uses AWS Secrets Manager as its datasource.

This can be configured with:

 * `.secretsManager(String secretName, String region)` - uses default AWS client to query Secrets Manager.
 * `.secretsManager(String secretName, String region, AWSSecretsManager client)` - uses provided client to query Secrets Manager. Use this if you want to provide custom client behaviour e.g. using a specific set of credentials or instance-role.

**NOTE**: This configuration source only supports key-value pairs as a return type - plaintext secrets will be ignored.

```java
Configuration config = Configuration.of(
    secretsManager("my-secret", "eu-west-1") // { "username": "user", "password": "secret" }
); 

Optional<String> password = config.get("username"); // Optional[user]
Optional<String> password = config.get("password"); // Optional[secret]
```

## Adding custom Configuration Sources

You can define your own sources of configuration by implementing the `ConfigurationSource` functional interface.

The contract between this and the `Configuration` class is:
 * A `ConfigurationSource` will not throw exceptions when queried.
 * A `ConfigurationSource` will return `null` if the requested key cannot be found or other error occurs.
 * A `ConfigurationSource` will return a `String` representing the value of a successful lookup.

For example, a custom `ConfigurationSource` that returns a different random string every time it's queried:

```java
import java.util.UUID;

public class RandomUUIDSource implements ConfigurationSource {
    @Override
    public String get(String key) {
        return UUID.randomUUID().toString();
    }
}
```

**NOTE**: Don't actually do this.

## Contributing

### Code of Conduct

Everyone interacting with this project is required to follow the [Code of Conduct](./CODE_OF_CONDUCT.md).

### Getting Started

You'll need Maven and Java 8+ installed

```bash
$ git clone git@github.com:unruly/java-config-fallback.git
$ mvn clean install
```

You can run `mvn clean test` in the project root to run JUnit tests.

### Releasing a Change

- To release a new version:
  - Run `mvn release:prepare release:perform` - NOTE: this requires signing the release with a GPG key.

## Design Decisions

There are already libraries (such as [properlty](https://github.com/ufoscout/properlty)) that do something very similar — why build our own?

 * **Focus on cleanliness and simplicity**: We aim to keep the core library at fewer than 100 lines of code.
 
 * **Architecting for extensibility**: the `ConfigurationSource` entry point is used to retrieve a specific value for a key, not to load all possible values into memory. This opens the door to providers for external services that operate on a request-by-request basis.
 
 * **Use modern Java features**: we designed `ConfigurationSource` as a functional interface, so custom providers can be defined in-line as lambda expressions:
```java
Configuration config = Configuration.of(
  (key) -> "always-this-value"
);
```

 * **Short-circuit querying**: Incorporating `.orElseGet(() -> ...)` into the composition of the sources stops the query being evaluated if a value is found “higher up”. For services where the call is non-trivial or expensive, this is much more efficient.
 
 * **Do one thing well**: The library doesn’t coerce types (opting for String key, String value) or assume behaviour if a source cannot be queried (missing file or slow network call). This is up to the user to decide, and simplifies a lot of our other design choices.
 
 * **Eat our own dog-food**: This library is in use at Unruly, helping us transition our app configuration and advance our architecture. We don’t want to release anything we wouldn’t use ourselves.

## License
 
This project is available as open source under the terms of the [MIT License](./LICENSE).

