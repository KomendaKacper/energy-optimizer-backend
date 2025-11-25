package com.codibly.task.service;

import com.codibly.task.dto.EnergyMixDto;
import com.codibly.task.dto.GenerationData;
import com.codibly.task.dto.GenerationData.ApiData;
import com.codibly.task.dto.GenerationData.MixElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EnergyMixServiceTest {

    @Mock
    private GenerationApi generationApi;

    @InjectMocks
    private EnergyMixService energyMixService;

    private ApiData createApiData(String from, double gas, double wind) {
        String to = Instant.parse(from).plus(30, ChronoUnit.MINUTES).toString();

        return new ApiData(from, to, List.of(
                new MixElement("gas", gas),
                new MixElement("wind", wind)
        ));
    }

    @Test
    void findOptimalChargingWindow_shouldReturnBestWindowForGivenDuration() {
        int durationHours = 2;

        List<ApiData> mockDataList = List.of(
                createApiData("2025-11-21T00:00:00Z", 80.0, 20.0),
                createApiData("2025-11-21T00:30:00Z", 60.0, 40.0),
                createApiData("2025-11-21T01:00:00Z", 40.0, 60.0),
                createApiData("2025-11-21T01:30:00Z", 20.0, 80.0),
                createApiData("2025-11-21T02:00:00Z", 30.0, 70.0),
                createApiData("2025-11-21T02:30:00Z", 50.0, 50.0)
        );

        when(generationApi.getGenerationMix(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new GenerationData(mockDataList));

        EnergyMixDto.OptimalChargingWindow result = energyMixService.findOptimalChargingWindow(durationHours);

        assertEquals("2025-11-21 01:00", result.startDate(), "Nieprawidłowy czas rozpoczęcia optymalnego okna.");
        assertEquals("2025-11-21 03:00", result.endDate(), "Nieprawidłowy czas zakończenia optymalnego okna.");
        assertEquals(65.0, result.averageCleanEnergyPercentage(), 0.01, "Nieprawidłowy procent czystej energii.");
    }

    @Test
    void findOptimalChargingWindow_shouldThrowExceptionForInvalidDuration() {
        assertThrows(IllegalArgumentException.class, () -> energyMixService.findOptimalChargingWindow(0));
        assertThrows(IllegalArgumentException.class, () -> energyMixService.findOptimalChargingWindow(7));
    }
}