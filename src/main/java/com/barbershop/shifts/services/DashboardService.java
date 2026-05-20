package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.dashboard.DashboardResponse;
import com.barbershop.shifts.dtos.dashboard.ClientDashboardResponse;

import java.time.LocalDate;
import java.time.YearMonth;

public interface DashboardService {
    DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate);
    ClientDashboardResponse getClientDashboard(Long clientId, YearMonth month);
}
