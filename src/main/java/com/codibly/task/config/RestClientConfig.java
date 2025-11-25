package com.codibly.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${carbon.intensity.api.base-url}")
    private String baseUrl;

    @Bean
    public RestClient energyApiRestClient(RestClient.Builder builder) {
        return builder.baseUrl(baseUrl).build();
    }
}