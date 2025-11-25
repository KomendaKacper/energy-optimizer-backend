package com.codibly.task.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record EnergyMixDto(
        LocalDate date,
        Map<String, Double> averageGeneration,
        double cleanEnergyPercentage
) {
    private static final List<String> CLEAN_ENERGY_SOURCES = List.of(
            "biomass", "nuclear", "hydro", "wind", "solar"
    );

    public static EnergyMixDto fromIntervals(LocalDate date, List<Map<String, Double>> halfHourlyIntervals) {
        if (halfHourlyIntervals == null || halfHourlyIntervals.isEmpty()) {
            return new EnergyMixDto(date, Map.of(), 0.0);
        }

        Map<String, Double> totalGeneration = halfHourlyIntervals.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(
                        () -> new java.util.HashMap<String, Double>(),
                        (map, entry) -> map.merge(entry.getKey(), entry.getValue(), Double::sum),
                        (map1, map2) -> map2.forEach((k, v) -> map1.merge(k, v, Double::sum))
                );

        Map<String, Double> averageGeneration = totalGeneration.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> round(entry.getValue() / halfHourlyIntervals.size())
                ));

        double totalAverage = averageGeneration.values().stream().mapToDouble(Double::doubleValue).sum();

        double cleanEnergyTotalAverage = averageGeneration.entrySet().stream()
                .filter(entry -> CLEAN_ENERGY_SOURCES.contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        double cleanEnergyPercentage = totalAverage > 0
                ? round((cleanEnergyTotalAverage / totalAverage) * 100)
                : 0.0;

        return new EnergyMixDto(date, averageGeneration, cleanEnergyPercentage);
    }

    public static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public record OptimalChargingWindow(
            String startDate,
            String endDate,
            double averageCleanEnergyPercentage
    ) {}
}