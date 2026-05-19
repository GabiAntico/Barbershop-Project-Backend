package com.barbershop.shifts.dtos.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardResponse {
    private RevenueStats revenue;
    private AttendanceStats attendance;

    @Data
    public static class RevenueStats {
        private BigDecimal totalPaidAmount;
        private Long paidVisitsCount;
        private BigDecimal averageTicket;
    }

    @Data
    public static class AttendanceStats {
        private Long completed;
        private Long cancelled;
        private Long missed;
        private Long futurePending;
        private Long totalEvaluated;
        private BigDecimal attendanceRate;
    }
}
