package com.barbershop.shifts.dtos.context;

import lombok.Data;

@Data
public class CreateBranchRequest {
    private String name;
    private String address;
    private String timeZone;
}
