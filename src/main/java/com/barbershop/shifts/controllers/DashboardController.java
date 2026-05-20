package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.dashboard.ClientDashboardResponse;
import com.barbershop.shifts.dtos.dashboard.DashboardResponse;
import com.barbershop.shifts.services.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getDashboard(startDate, endDate));
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientDashboardResponse> getClientDashboard(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(dashboardService.getClientDashboard(clientId, month));
    }
}
