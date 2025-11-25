package com.codibly.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerationData(
        List<ApiData> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiData(
            String from,
            String to,
            List<MixElement> generationmix
    ) {
        public Map<String, Double> getMixMap() {
            return generationmix.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            MixElement::fuel,
                            MixElement::perc
                    ));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MixElement(
            String fuel,
            double perc
    ) {}
}