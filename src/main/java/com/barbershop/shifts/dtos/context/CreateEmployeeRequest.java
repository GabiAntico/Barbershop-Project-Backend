package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class CreateEmployeeRequest {
    private String displayName;
    private String email;
    private String temporaryPassword;
    private List<Long> branchIds;
}
