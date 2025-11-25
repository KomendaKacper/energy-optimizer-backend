package com.codibly.task.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyMixDtoTest {

    private final LocalDate TEST_DATE = LocalDate.of(2024, 1, 1);

    @Test
    void testFromIntervals_shouldCalculateAverageAndCleanEnergyCorrectly() {
        Map<String, Double> mix1 = Map.of(
                "gas", 40.0,
                "wind", 30.0,
                "coal", 20.0,
                "nuclear", 10.0
        );
        Map<String, Double> mix2 = Map.of(
                "gas", 60.0,
                "wind", 10.0,
                "coal", 10.0,
                "nuclear", 20.0
        );

        List<Map<String, Double>> intervals = List.of(mix1, mix2);

        EnergyMixDto result = EnergyMixDto.fromIntervals(TEST_DATE, intervals);

        assertNotNull(result);
        assertEquals(TEST_DATE, result.date());

        Map<String, Double> expectedAverage = Map.of(
                "gas", 50.00,
                "wind", 20.00,
                "coal", 15.00,
                "nuclear", 15.00
        );
        assertEquals(expectedAverage, result.averageGeneration());
        assertEquals(35.00, result.cleanEnergyPercentage(), 0.01, "Procent czystej energii powinien być 35.00%");
    }

    @Test
    void testFromIntervals_whenAllClean() {
        Map<String, Double> mix = Map.of(
                "solar", 50.0,
                "hydro", 50.0
        );

        EnergyMixDto result = EnergyMixDto.fromIntervals(TEST_DATE, List.of(mix));

        assertEquals(100.00, result.cleanEnergyPercentage(), 0.01, "Procent powinien wynosić 100.00%");
    }

    @Test
    void testFromIntervals_whenEmptyList() {
        EnergyMixDto result = EnergyMixDto.fromIntervals(TEST_DATE, List.of());

        assertNotNull(result);
        assertTrue(result.averageGeneration().isEmpty());
        assertEquals(0.0, result.cleanEnergyPercentage());
    }

    @Test
    void testRound_shouldRoundToTwoDecimalPlaces() {
        assertEquals(12.35, EnergyMixDto.round(12.34567));
        assertEquals(5.00, EnergyMixDto.round(5.0));
        assertEquals(7.89, EnergyMixDto.round(7.889));
    }
}