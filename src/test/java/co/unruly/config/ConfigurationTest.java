package co.unruly.config;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static co.unruly.config.Configuration.map;
import static co.unruly.config.Configuration.properties;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigurationTest {

    @Test
    public void shouldReturnEmptyOptionalIfValueNotFound() {
        Map<String, String> map = Collections.emptyMap();

        Configuration config = Configuration.from(map::get);

        assertThat(config.get("some-variable"), is(Optional.empty()));
    }

    @Test
    public void shouldReturnOptionalIfValueIsPresent() {
        Map<String, String> input = new HashMap<>();

        input.put("some-variable", "dfsadad");

        Configuration config = Configuration.from(input::get);

        assertThat(config.get("some-variable"), is(Optional.of("dfsadad")));
    }

    @Test
    public void shouldReadFromFileFirstIfValueIsPresent() {
        Map<String, String> input = new HashMap<>();
        input.put("some-variable", "dfsadad");

        Configuration config = Configuration
            .from(properties("src/test/resources/test.properties"))
            .or(map(input));

        assertThat(config.get("some-variable"), is(Optional.of("blah")));
    }

    @Test(expected = ConfigurationSetUpException.class)
    public void shouldThrowExceptionIfProblemOpeningPropertiesFile() {
        Configuration
            .from(properties("src/test/resources/doesNotExist.properties"));
    }

}
