package co.unruly.config;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static co.unruly.config.Configuration.environment;
import static co.unruly.config.Configuration.map;
import static co.unruly.config.Configuration.properties;
import static co.unruly.matchers.OptionalMatchers.contains;
import static co.unruly.matchers.OptionalMatchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ConfigurationTest {

    @Test
    public void shouldReturnEmptyOptionalIfValueNotFound() {
        Map<String, String> input = Collections.emptyMap();

        Configuration config = Configuration.from(map(input));

        assertThat(config.get("some-variable"), is(empty()));
    }

    @Test
    public void shouldReturnOptionalIfValueIsPresent() {
        Map<String, String> input = new HashMap<>();

        input.put("some-variable", "dfsadad");

        Configuration config = Configuration.from(map(input));

        assertThat(config.get("some-variable"), contains("dfsadad"));
    }

    @Test
    public void shouldReadFromFileFirstIfValueIsPresent() {
        Map<String, String> input = new HashMap<>();
        input.put("some-variable", "dfsadad");

        Configuration config = Configuration
            .from(properties("src/test/resources/test.properties"))
            .or(map(input));

        assertThat(config.get("some-variable"), contains("blah"));
    }

    @Test
    public void shouldSupportVarargsOrdering() {
        Map<String, String> input = new HashMap<>();
        input.put("some-variable", "dfsadad");

        Configuration config = Configuration.of(
            properties("src/test/resources/test.properties"),
            map(input)
        );

        assertThat(config.get("some-variable"), contains("blah"));
    }

    @Test
    public void shouldThrowExceptionIfProblemOpeningPropertiesFile() {
        Configuration config = Configuration
            .from(properties("src/test/resources/doesNotExist.properties"));

        assertThat(config.get("anything"), is(empty()));
    }

    @Test
    public void shouldNotCallSubsequentSourcesIfEarlierSourceProvidesValue() {
        Map<String, String> input = new HashMap<>();
        input.put("some-variable", "dfsadad");

        ConfigurationSource secondarySource = mock(ConfigurationSource.class);

        Configuration config = Configuration.of(
                map(input),
                secondarySource
        );

        assertThat(config.get("some-variable"), contains("dfsadad"));
        verifyZeroInteractions(secondarySource);
    }

    // VARIABLE=foo is set via maven-surefire-plugin in the POM

    @Test
    public void shouldUseEnvironmentVariables_ToUpperCase() {
        Configuration config = Configuration.of(
            environment()
        );

        assertThat(config.get("variable"), contains("foo"));
    }

    @Test
    public void shouldUseEnvironmentVariables_ExactCase() {
        Configuration config = Configuration.of(
                environment()
        );

        assertThat(config.get("VARIABLE"), contains("foo"));
    }

}
