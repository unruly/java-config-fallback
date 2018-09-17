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
import java.util.Optional;

public class SecretsManager implements ConfigurationSource {

    private final String secretName;
    private final String region;
    private final AWSSecretsManager client;
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
        if (credentials == null) {
            credentials = fetchCredentials();
        }

        return credentials.get(key);
    }

    private Map<String, String> fetchCredentials() {
        final Map<String, String> emptyResults = new HashMap<>();

        return getSecretValueFromRequest()
                .map(GetSecretValueResult::getSecretString)
                .flatMap(this::parseJSON)
                .orElse(emptyResults);
    }


    private AWSSecretsManager setupClient() {
        return AWSSecretsManagerClientBuilder.standard()
                .withRegion(this.region)
                .build();
    }

    private Optional<GetSecretValueResult> getSecretValueFromRequest() {
        return getSecretValue(createRequest());
    }

    private GetSecretValueRequest createRequest() {
        return new GetSecretValueRequest().withSecretId(this.secretName);
    }

    private Optional<GetSecretValueResult> getSecretValue(GetSecretValueRequest getSecretValueRequest) {
        try {
            return Optional.ofNullable(client.getSecretValue(getSecretValueRequest));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<Map<String, String>> parseJSON(String jsonInput) {
        final ObjectMapper mapper = new ObjectMapper();
        final TypeReference<Map<String, String>> parseAsMap = new TypeReference<Map<String, String>>() {};

        try {
            return Optional.ofNullable(mapper.readValue(jsonInput, parseAsMap));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
