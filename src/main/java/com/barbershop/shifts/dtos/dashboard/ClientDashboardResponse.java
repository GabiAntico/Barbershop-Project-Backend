package com.barbershop.shifts.dtos.dashboard;

import lombok.Data;

@Data
public class ClientDashboardResponse {
    private ClientStats client;
    private DashboardResponse.AttendanceStats attendance;
    private DashboardResponse.RevenueStats selectedMonthRevenue;
    private DashboardResponse.RevenueStats historicalRevenue;

    @Data
    public static class ClientStats {
        private Long id;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String email;
    }
}
