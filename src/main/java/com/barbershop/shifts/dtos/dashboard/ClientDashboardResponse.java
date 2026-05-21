package com.barbershop.shifts.dtos.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientDashboardResponse {
    private ClientStats client;
    private DashboardResponse.AttendanceStats attendance;
    private DashboardResponse.RevenueStats selectedMonthRevenue;
    private DashboardResponse.RevenueStats historicalRevenue;
    private VisitFrequencyStats visitFrequency;

    @Data
    public static class ClientStats {
        private Long id;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String email;
        private String notes;
    }

    @Data
    public static class VisitFrequencyStats {
        private LocalDateTime lastVisitAt;
        private Integer averageDaysBetweenVisits;
        private Long visitsCount;
    }
}
