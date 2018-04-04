package co.unruly.config;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static co.unruly.config.ConfigurationSource.FIND_NOTHING;

public class Configuration {

    private final ConfigurationSource func;

    public Configuration() {
        this(FIND_NOTHING);
    }

    public Configuration(ConfigurationSource map) {
        this.func = map;
    }

    public static Configuration from(ConfigurationSource func) {
        return new Configuration(func);
    }

    public Optional<String> get(String s) {
        return Optional.ofNullable(func.get(s));
    }

    public Configuration or(ConfigurationSource next) {
        return new Configuration(this.func.or(next));
    }

    public static ConfigurationSource map(Map<String, String> map){
        return map::get;
    };

    public static ConfigurationSource properties(String s) {
        Properties properties = new Properties();

        try {
            properties.load(new FileReader(s));
        } catch (IOException e) {
           throw new ConfigurationSetUpException(e);
        }

        return properties::getProperty;
    }

    public static Configuration of(ConfigurationSource... sources) {
        return new Configuration(Stream.of(sources).reduce(FIND_NOTHING, ConfigurationSource::or));
    }
}

@FunctionalInterface
interface ConfigurationSource {

    ConfigurationSource FIND_NOTHING = key -> null;

    String get(String key);

    default ConfigurationSource or(ConfigurationSource source) {
        return (key) -> Optional
            .ofNullable(this.get(key))
            .orElse(source.get(key));
    }
}
