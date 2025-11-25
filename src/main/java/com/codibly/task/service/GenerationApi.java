package com.codibly.task.service;

import com.codibly.task.dto.GenerationData;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GenerationApi {

    private final RestClient restClient;
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    public GenerationApi(RestClient restClient) {
        this.restClient = restClient;
    }

    public GenerationData getGenerationMix(LocalDateTime from, LocalDateTime to) {
        String fromStr = from.format(API_DATE_FORMATTER);
        String toStr = to.format(API_DATE_FORMATTER);
        String path = String.format("/%s/%s", fromStr, toStr);

        System.out.println("DEBUG: Pytam API o ścieżkę: " + path);

        try {
            return restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(GenerationData.class);
        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania danych z API: " + e.getMessage());
            throw new RuntimeException("Nie udało się pobrać danych miksu energetycznego. Sprawdź logi serwera API (np. problem z zakresem dat).", e);
        }
    }
}