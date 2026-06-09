package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class WorkContextResponse {
    private Long barbershopId;
    private String barbershopName;
    private String userRole;
    private Boolean temporaryPassword;
    private List<BranchResponse> branches;
}
