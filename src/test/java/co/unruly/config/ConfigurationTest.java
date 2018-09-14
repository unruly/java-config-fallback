package co.unruly.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static co.unruly.config.Configuration.*;
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
    public void shouldFallBackToNextConfigurationSource() {

        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn("");

        Map<String, String> input = new HashMap<>();
        input.put("map-scenario-user", "from-a-map");

        Configuration config = Configuration.of(
                properties("src/test/resources/test.properties"),
                secretsManager("some_secret", "eu-west-1", awsMockClient),
                map(input)
        );

        assertThat(config.get("map-scenario-user"), contains("from-a-map"));
    }

    @Test
    public void shouldFallBackToAWSSecretsManagerConfigurationSource() {

        String unparsedJSON = "{\"secrets-scenario-user\": \"from-secrets-manager\", \"pass\": \"some_pass\"}";

        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn(unparsedJSON);

        Map<String, String> input = new HashMap<>();
        input.put("map-scenario-user", "from-a-map");

        Configuration config = Configuration.of(
                properties("src/test/resources/test.properties"),
                secretsManager("some_secret", "eu-west-1", awsMockClient),
                map(input)
        );

        assertThat(config.get("secrets-scenario-user"), contains("from-secrets-manager"));
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
    public void shouldReadFromSecretsManager() {
        String unparsedJSON = "{\"user\": \"some_user\", \"pass\": \"some_pass\"}";

        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn(unparsedJSON);

        Configuration config = Configuration.of(
                Configuration.secretsManager("some_secret", "eu-west-1", awsMockClient)
        );

        assertThat(config.get("user"), contains("some_user"));
        assertThat(config.get("pass"), contains("some_pass"));
    }

    @Test
    public void shouldReturnEmptyOptionalIfNotFoundInSecretsManager() {
        String region = "eu-west-1";

        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn("");


        Configuration config = Configuration.of(
                secretsManager("some_secret_that_does_not_exist", region)
        );

        assertThat(config.get("user"), is(empty()));
        assertThat(config.get("pass"), is(empty()));
    }

}
