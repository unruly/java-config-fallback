package co.unruly.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SecretsManagerTest {

    @Test
    public void shouldReturnSecretWhenItDoesExist() {
        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        String unparsedJSON = "{\"foo\": \"bar\"}";

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn(unparsedJSON);

        SecretsManager secretsManager = new SecretsManager("secret-name", "eu-west-1", awsMockClient);

        assertThat(secretsManager.get("foo"), is("bar"));
    }

    @Ignore
    @Test
    public void shouldNotReturnSecretWhenItDoesNotExist() {
        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        String unparsedJSON = "this is not JSON";

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn(unparsedJSON);

        SecretsManager secretsManager = new SecretsManager("secret-name", "eu-west-1", awsMockClient);

        assertThat(secretsManager.get("user"), is(""));
    }
}
