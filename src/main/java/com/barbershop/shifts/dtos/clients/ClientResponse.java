package com.barbershop.shifts.dtos.clients;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String documentNumber;
}
