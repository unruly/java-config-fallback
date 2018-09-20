package co.unruly.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static co.unruly.config.Configuration.*;
import static co.unruly.config.SecretsManagerTest.storeSecret;
import static co.unruly.matchers.OptionalMatchers.contains;
import static co.unruly.matchers.OptionalMatchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ConfigurationTest {

    @Test
    public void shouldReturnEmptyOptionalIfValueNotFound() {
        Map<String, String> input = Collections.emptyMap();
        Configuration config = Configuration.from(map(input));

        assertThat(config.get("some-variable"), is(empty()));
    }

    @Test
    public void shouldReturnDefaultValueIfProvidedAndValueNotFound() {
        Map<String, String> input = Collections.emptyMap();
        Configuration config = Configuration.from(map(input));

        assertThat(config.get("some-variable", "default-value"), is("default-value"));
    }

    @Test(expected = ConfigurationMissing.class)
    public void shouldThrowIfRequiredValueNotFound() {
        Map<String, String> input = Collections.emptyMap();
        Configuration config = Configuration.from(map(input));

        config.require("some-variable");
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
    public void shouldFallBackToNextConfigurationSource_Map() {
        Map<String, String> input = new HashMap<>();
        input.put("map-scenario-user", "from-a-map");

        Configuration config = Configuration.of(
                map(new HashMap<>()),
                map(input)
        );

        assertThat(config.get("map-scenario-user"), contains("from-a-map"));
    }

    @Test
    public void shouldFallBackToNextConfigurationSource_Properties() {
        Configuration config = Configuration.of(
                properties("this-file-does-not-exist.properties"),
                properties("src/test/resources/test.properties")
        );

        assertThat(config.get("some-variable"), contains("blah"));
    }

    @Test
    public void shouldFallBackToNextConfigurationSource_SecretsManager() {
        Configuration config = Configuration.of(
                secretsManager("my-secret-1", "eu-west-1", storeSecret("not actually JSON")),
                secretsManager("my-secret-2", "eu-west-1", storeSecret("{\"my-key\":\"my-value\"}"))
        );

        assertThat(config.get("my-key"), contains("my-value"));
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

    @Test
    public void shouldReturnEmptyOptionalIfNotFoundInSecretsManager() {
        AWSSecretsManager awsMockClient = storeSecret("");

        Configuration config = Configuration.of(
                secretsManager("some_secret_that_does_not_exist", "eu-west-1", awsMockClient)
        );

        assertThat(config.get("user"), is(empty()));
        assertThat(config.get("pass"), is(empty()));
    }

    @Test
    public void shouldUseSystemProperties() {
        System.setProperty("my.system.property", "my-value");

        Configuration config = Configuration.of(systemProperties());

        assertThat(config.get("my.system.property"), contains("my-value"));
    }

    @Test
    public void shouldUseSystemProperties_BehavesCorrectlyWhenMissing() {
        System.clearProperty("my.system.property");

        Configuration config = Configuration.of(systemProperties());

        assertThat(config.get("my.system.property"), is(empty()));
    }

}
