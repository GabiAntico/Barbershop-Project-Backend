package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeScheduleResponse {
    private Long employeeId;
    private Long branchId;
    private List<EmployeeScheduleDayResponse> days;
}
