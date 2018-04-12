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

// look in properties file first, else look in the map.
String username = config.get("username"); 
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