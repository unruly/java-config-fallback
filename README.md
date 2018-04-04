# java-config-fallback

A lightweight library to facilitate extracting configuration from code into configuration files.

## Usage

### With a basic in-code Map<>

```java
Map<String, String> credentials = new HashMap<>();
credentials.put("username","foo");
credentials.put("password","this-is-a-secret!");

Configuration config = Configuration
  .from(map(credentials));

String username = config.get("username"); // Optional[foo]
String password = config.get("password"); // Optional[this-is-a-secret!]
```
### Using a .properties file, falling back to a Map<>

```java
Map<String, String> credentials = new HashMap<>();
credentials.put("username","foo");
credentials.put("password","this-is-a-secret!");

Configuration config = Configuration
  .from(file("/etc/my-app/config.properties")) // password=new-password
  .or(map(credentials));

String username = config.get("username"); // Optional[foo]
String password = config.get("password"); // Optional[new-password]
```

## Supported ConfigurationSource types

 * `map(Map<>)` - an in-code map of keys to values
 * `properties(String pathToFile)` - a .properties file in the filesystem
