package co.unruly.config;

import java.util.Optional;

@FunctionalInterface
public interface ConfigurationSource {

    ConfigurationSource FIND_NOTHING = key -> null;

    String get(String key);

    default ConfigurationSource or(ConfigurationSource source) {
        return (key) -> Optional
            .ofNullable(this.get(key))
            .orElse(source.get(key));
    }
}
