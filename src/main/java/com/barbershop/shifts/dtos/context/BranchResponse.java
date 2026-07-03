package com.barbershop.shifts.dtos.context;

import lombok.Data;

@Data
public class BranchResponse {
    private Long id;
    private String name;
    private String address;
    private String timeZone;
}
