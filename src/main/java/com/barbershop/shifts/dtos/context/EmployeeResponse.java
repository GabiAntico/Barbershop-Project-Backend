package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeResponse {
    private Long id;
    private String displayName;
    private String email;
    private String role;
    private Boolean temporaryPassword;
    private List<BranchResponse> branches;
}
