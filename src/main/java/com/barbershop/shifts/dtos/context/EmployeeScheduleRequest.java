package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeScheduleRequest {
    private Long branchId;
    private List<EmployeeScheduleDayRequest> days;
}
