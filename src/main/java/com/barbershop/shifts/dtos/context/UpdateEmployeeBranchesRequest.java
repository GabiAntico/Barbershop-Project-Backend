package com.barbershop.shifts.dtos.context;

import lombok.Data;

import java.util.List;

@Data
public class UpdateEmployeeBranchesRequest {
    private List<Long> branchIds;
}
