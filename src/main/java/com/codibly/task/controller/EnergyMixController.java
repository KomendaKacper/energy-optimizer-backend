package com.codibly.task.controller;

import com.codibly.task.dto.EnergyMixDto;
import com.codibly.task.dto.EnergyMixDto.OptimalChargingWindow;
import com.codibly.task.service.EnergyMixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin(origins = "https://energy-optimizer-frontend.onrender.com")
public class EnergyMixController {

    private final EnergyMixService energyMixService;

    public EnergyMixController(EnergyMixService energyMixService) {
        this.energyMixService = energyMixService;
    }

    @GetMapping("/daily-mix")
    public List<EnergyMixDto> getDailyAverageMix() {
        return energyMixService.getDailyAverageMix();
    }

    @GetMapping("/optimal-charge")
    public ResponseEntity<?> findOptimalChargingWindow(@RequestParam int durationHours) {
        try {
            OptimalChargingWindow result = energyMixService.findOptimalChargingWindow(durationHours);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Błąd serwera: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Wystąpił wewnętrzny błąd serwera.");
        }
    }
}