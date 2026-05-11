package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.settings.AppSettingsRequest;
import com.barbershop.shifts.dtos.settings.AppSettingsResponse;
import com.barbershop.shifts.dtos.settings.ScheduleSettingsRequest;
import com.barbershop.shifts.dtos.settings.ScheduleSettingsResponse;
import com.barbershop.shifts.services.AppSettingsService;
import com.barbershop.shifts.services.ScheduleSettingsService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/settings")
public class AppSettingsController {

    private final AppSettingsService appSettingsService;
    private final ScheduleSettingsService scheduleSettingsService;

    public AppSettingsController(AppSettingsService appSettingsService, ScheduleSettingsService scheduleSettingsService) {
        this.appSettingsService = appSettingsService;
        this.scheduleSettingsService = scheduleSettingsService;
    }

    @GetMapping
    public ResponseEntity<AppSettingsResponse> getSettings() {
        return ResponseEntity.ok(appSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<AppSettingsResponse> updateSettings(@Valid @RequestBody AppSettingsRequest request) {
        return ResponseEntity.ok(appSettingsService.updateSettings(request));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ScheduleSettingsResponse> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(scheduleSettingsService.getSchedule(date));
    }

    @GetMapping("/schedule/range")
    public ResponseEntity<ScheduleSettingsResponse> getScheduleRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(scheduleSettingsService.getScheduleRange(startDate, endDate));
    }

    @GetMapping("/schedule/default")
    public ResponseEntity<ScheduleSettingsResponse> getDefaultSchedule() {
        return ResponseEntity.ok(scheduleSettingsService.getDefaultSchedule());
    }

    @PutMapping("/schedule")
    public ResponseEntity<ScheduleSettingsResponse> updateSchedule(@Valid @RequestBody ScheduleSettingsRequest request) {
        return ResponseEntity.ok(scheduleSettingsService.updateSchedule(request));
    }
}
