package com.codibly.task.service;

import com.codibly.task.dto.EnergyMixDto;
import com.codibly.task.dto.GenerationData;
import com.codibly.task.dto.EnergyMixDto.OptimalChargingWindow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class EnergyMixService {

    private final GenerationApi generationApi;

    private static final List<String> CLEAN_ENERGY_SOURCES = List.of(
            "biomass", "nuclear", "hydro", "wind", "solar"
    );

    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EnergyMixService(GenerationApi generationApi) {
        this.generationApi = generationApi;
    }

    public List<EnergyMixDto> getDailyAverageMix() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int minute = now.getMinute() < 30 ? 0 : 30;
        LocalDateTime from = now.withMinute(minute).withSecond(0).withNano(0);

        LocalDate dayAfterTomorrow = today.plusDays(2);
        LocalDateTime to = dayAfterTomorrow.plusDays(1).atStartOfDay().withHour(0);

        GenerationData data = generationApi.getGenerationMix(from, to);

        Map<LocalDate, List<Map<String, Double>>> groupedByDate = data.data().stream()
                .collect(Collectors.groupingBy(
                        apiData -> LocalDateTime.parse(apiData.from(), DateTimeFormatter.ISO_DATE_TIME).toLocalDate(),
                        Collectors.mapping(GenerationData.ApiData::getMixMap, Collectors.toList())
                ));

        return IntStream.range(0, 3)
                .mapToObj(today::plusDays)
                .map(date -> EnergyMixDto.fromIntervals(date, groupedByDate.getOrDefault(date, List.of())))
                .collect(Collectors.toList());
    }

    public OptimalChargingWindow findOptimalChargingWindow(int durationHours) {
        if (durationHours < 1 || durationHours > 6) {
            throw new IllegalArgumentException("Długość okna ładowania musi być w zakresie 1 do 6 godzin.");
        }

        final int windowSizeIntervals = durationHours * 2;

        LocalDate tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

        LocalDateTime from = tomorrow.atStartOfDay().withHour(0).withMinute(0);
        LocalDateTime to = dayAfterTomorrow.plusDays(1).atStartOfDay().withHour(0).withMinute(0);

        GenerationData data = generationApi.getGenerationMix(from, to);
        List<GenerationData.ApiData> allIntervals = data.data();

        if (allIntervals.size() < windowSizeIntervals) {
            throw new IllegalStateException("Za mało danych prognozowanych do obliczenia okna ładowania.");
        }

        OptimalChargingWindow bestWindow = null;
        double maxCleanEnergy = -1.0;

        for (int i = 0; i <= allIntervals.size() - windowSizeIntervals; i++) {
            List<GenerationData.ApiData> currentWindow = allIntervals.subList(i, i + windowSizeIntervals);

            double currentCleanEnergyPercentage = calculateWindowCleanEnergyPercentage(currentWindow);

            if (currentCleanEnergyPercentage > maxCleanEnergy) {
                maxCleanEnergy = currentCleanEnergyPercentage;

                String startTime = currentWindow.get(0).from();
                String endTime = currentWindow.get(currentWindow.size() - 1).to();

                bestWindow = new OptimalChargingWindow(
                        formatUtcToLocal(startTime),
                        formatUtcToLocal(endTime),
                        currentCleanEnergyPercentage
                );
            }
        }

        if (bestWindow == null) {
            throw new IllegalStateException("Nie udało się znaleźć optymalnego okna ładowania. Sprawdź logi API.");
        }

        return bestWindow;
    }

    private double calculateWindowCleanEnergyPercentage(List<GenerationData.ApiData> window) {
        List<Double> cleanEnergyPercents = window.stream().map(interval -> {
            Map<String, Double> mix = interval.getMixMap();
            double total = mix.values().stream().mapToDouble(Double::doubleValue).sum();

            double cleanTotal = mix.entrySet().stream()
                    .filter(entry -> CLEAN_ENERGY_SOURCES.contains(entry.getKey()))
                    .mapToDouble(Map.Entry::getValue)
                    .sum();

            return total > 0 ? (cleanTotal / total) * 100 : 0.0;
        }).toList();

        double averagePercentage = cleanEnergyPercents.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return EnergyMixDto.round(averagePercentage);
    }

    private String formatUtcToLocal(String utcTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(utcTime, DateTimeFormatter.ISO_DATE_TIME);
        return localDateTime.format(OUTPUT_FORMATTER);
    }
}