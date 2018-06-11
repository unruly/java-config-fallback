# java-config-fallback

[![Build Status](https://travis-ci.org/unruly/java-config-fallback.svg?branch=master)](https://travis-ci.org/unruly/java-config-fallback)

A lightweight library to facilitate extracting configuration from code into configuration files.

## Install from Maven Central

```xml
<dependency>
    <groupId>co.unruly</groupId>
    <artifactId>java-config-fallback</artifactId>
    <version>0.1.5</version>
</dependency>
```

## Usage

```java
Map<String, String> credentials = new HashMap<>();
credentials.put("username","foo");
credentials.put("password","this-is-a-secret!");

Configuration config = Configuration.of(
    properties("/etc/my-app-config.properties"),
    map(credentials)
);

// look in properties file first, else look in the map,
// and if still not found, return Optional.empty()
Optional<String> username = config.get("username"); 
```

You can also provide a use-site default value:

```java
String username = config.get("username", "Titus Andromedon");
```

Or state that a value is _required_, and throw a `ConfigurationMissing` exception if if is not defined:

```java
String username = config.require("username");
```

## Configuration Sources

### In-code map

`map(Map<String, String>)` - an in-code map of keys to values

```java
Map<String, String> credentials = new HashMap<>();
credentials.put("username","foo");
credentials.put("password","this-is-a-secret!");

Configuration config = Configuration.of(map(credentials));

String username = config.get("username"); // Optional[foo]
String password = config.get("password"); // Optional[this-is-a-secret!]
```

### Properties file

`properties(String pathToFile)` - a .properties file in the filesystem

```java
Configuration config = Configuration.of(
    properties("/etc/my-app/config.properties"),  // password=new-password
);

String password = config.get("password"); // Optional[new-password]
```

### Environment variables

`environment()` - environment variables set for this application.

This assumes environment variables are UPPER_CASE and converts queried keys to UPPER_CASE before lookup.

```java
// With VARIABLE=foo set

Configuration config = Configuration.of(
    environment()
);

String password = config.get("VARIABLE"); // Optional[foo]
String password = config.get("variable"); // Same as above
```
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
