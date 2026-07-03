package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class WorkContextResponse {
    private Long barbershopId;
    private String barbershopName;
    private Long userId;
    private String displayName;
    private String email;
    private String userRole;
    private Boolean temporaryPassword;
    private String profileImageUrl;
    private List<BranchResponse> branches;
}
