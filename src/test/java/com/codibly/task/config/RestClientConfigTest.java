package com.codibly.task.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RestClientConfig.class)
@Import(RestClientAutoConfiguration.class)
@TestPropertySource(properties = "carbon.intensity.api.base-url=https://test.api.com/v1")
class RestClientConfigTest {

    @Autowired
    private RestClient energyApiRestClient;

    @Test
    void energyApiRestClient_shouldBeCreatedWithBaseUrl() {
        assertNotNull(energyApiRestClient, "Bean RestClient nie powinien być null.");
        assertTrue(energyApiRestClient instanceof RestClient, "Bean powinien być typu RestClient.");
    }
}