package com.codibly.task.controller;

import com.codibly.task.dto.EnergyMixDto;
import com.codibly.task.dto.EnergyMixDto.OptimalChargingWindow;
import com.codibly.task.service.EnergyMixService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnergyMixController.class)
class EnergyMixControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnergyMixService energyMixService;


    @Test
    void getDailyAverageMix_shouldReturnMixListAndHttpStatus200() throws Exception {
        List<EnergyMixDto> mockList = List.of(
                new EnergyMixDto(LocalDate.of(2025, 1, 1), Map.of("wind", 30.0), 30.0),
                new EnergyMixDto(LocalDate.of(2025, 1, 2), Map.of("gas", 50.0), 10.0)
        );
        when(energyMixService.getDailyAverageMix()).thenReturn(mockList);

        mockMvc.perform(get("/api/energy/daily-mix")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$[0].cleanEnergyPercentage").value(30.0));
    }

    @Test
    void findOptimalChargingWindow_shouldReturnOptimalWindowAndHttpStatus200() throws Exception {
        int duration = 3;
        OptimalChargingWindow mockWindow = new OptimalChargingWindow(
                "2025-11-21 02:00",
                "2025-11-21 05:00",
                75.50
        );
        when(energyMixService.findOptimalChargingWindow(duration)).thenReturn(mockWindow);

        mockMvc.perform(get("/api/energy/optimal-charge")
                        .param("durationHours", String.valueOf(duration))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.startDate").value("2025-11-21 02:00"))
                .andExpect(jsonPath("$.averageCleanEnergyPercentage").value(75.50));
    }

    @Test
    void findOptimalChargingWindow_shouldReturn400ForInvalidDuration() throws Exception {
        int invalidDuration = 7;
        String errorMessage = "Długość okna ładowania musi być w zakresie 1 do 6 godzin.";

        when(energyMixService.findOptimalChargingWindow(invalidDuration))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(get("/api/energy/optimal-charge")
                        .param("durationHours", String.valueOf(invalidDuration))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void findOptimalChargingWindow_shouldReturn500ForServiceFailure() throws Exception {
        int duration = 2;
        when(energyMixService.findOptimalChargingWindow(duration))
                .thenThrow(new RuntimeException("API error"));

        mockMvc.perform(get("/api/energy/optimal-charge")
                        .param("durationHours", String.valueOf(duration))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Wystąpił wewnętrzny błąd serwera."));
    }
}