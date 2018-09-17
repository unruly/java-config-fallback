package co.unruly.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SecretsManagerTest {

    @Test
    public void shouldReturnSecretWhenItDoesExist() {
        AWSSecretsManager awsMockClient = storeSecret("{\"foo\": \"bar\"}");

        SecretsManager secretsManager = new SecretsManager("secret-name", "eu-west-1", awsMockClient);

        assertThat(secretsManager.get("foo"), is("bar"));
    }

    @Test
    public void shouldNotReturnSecretIfContentsArePlaintext() {
        AWSSecretsManager awsMockClient = storeSecret("this is not JSON");

        SecretsManager secretsManager = new SecretsManager("secret-name", "eu-west-1", awsMockClient);

        assertThat(secretsManager.get("user"), is(nullValue()));
    }

    @Test
    public void shouldNotMakeMoreThanOneRequestToAWSForASecret() {
        AWSSecretsManager awsMockClient = storeSecret("{\"foo\": \"bar\"}");

        SecretsManager secretsManager = new SecretsManager("secret-name", "eu-west-1", awsMockClient);

        secretsManager.get("user");
        secretsManager.get("user");
        secretsManager.get("user");

        verify(awsMockClient, times(1)).getSecretValue(any(GetSecretValueRequest.class));
    }

    @Test
    public void shouldHandleMissingSecretsGracefully() {
        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenThrow(
            new ResourceNotFoundException("Nothing here")
        );

        SecretsManager secretsManager = new SecretsManager("secret-name", "eu-west-1", awsMockClient);

        assertThat(secretsManager.get("user"), is(nullValue()));
    }

    public static AWSSecretsManager storeSecret(String secret) {
        AWSSecretsManager awsMockClient = mock(AWSSecretsManager.class);
        GetSecretValueResult mockResult = mock(GetSecretValueResult.class);

        when(awsMockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        when(mockResult.getSecretString()).thenReturn(secret);

        return awsMockClient;
    }
}
