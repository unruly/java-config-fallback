package co.unruly.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SecretsManager implements ConfigurationSource {

    private String secretName;
    private String region;
    private AWSSecretsManager client;
    private Map<String, String> credentials;

    public SecretsManager(String secretName, String region) {
        this.secretName = secretName;
        this.region = region;
        this.client = setupClient();
    }

    public SecretsManager(String secretName, String region, AWSSecretsManager client) {
        this.secretName = secretName;
        this.region = region;
        this.client = client;
    }

    @Override
    public String get(String key) {
        credentials = fetchCredentials();
        return credentials.get(key);
    }

    private AWSSecretsManager setupClient() {
        return AWSSecretsManagerClientBuilder.standard()
                .withRegion(this.region)
                .build();
    }

    private GetSecretValueResult getSecretValueFromRequest() {
        GetSecretValueRequest request = createRequest();

        return getSecretValue(request);
    }


    private Map<String, String> fetchCredentials() {

        GetSecretValueResult secretValue = getSecretValueFromRequest();

        if (secretValue.getSecretString() != null) {
            try {
                return parseJSON(secretValue);
            } catch (IOException e) {}
        }

        return new HashMap<>();
    }

    private GetSecretValueRequest createRequest() {
        return new GetSecretValueRequest().withSecretId(this.secretName);
    }

    private GetSecretValueResult getSecretValue(GetSecretValueRequest getSecretValueRequest) {
        GetSecretValueResult result;

        try {
            result = client.getSecretValue(getSecretValueRequest);
        } catch (ResourceNotFoundException e) {
            result = new GetSecretValueResult();
        }

        return result;
    }


    private Map<String, String> parseJSON(GetSecretValueResult getSecretValueResult) throws IOException {
        return new ObjectMapper().readValue(getSecretValueResult.getSecretString(), new TypeReference<Map<String, String>>(){});
    }
}
