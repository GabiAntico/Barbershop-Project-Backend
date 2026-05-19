package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.dashboard.DashboardResponse;

import java.time.LocalDate;

public interface DashboardService {
    DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate);
}
