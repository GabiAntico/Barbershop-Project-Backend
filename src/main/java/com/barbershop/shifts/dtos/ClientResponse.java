package com.barbershop.shifts.dtos;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String documentNumber;
}
