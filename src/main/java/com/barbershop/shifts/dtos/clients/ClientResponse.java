package com.barbershop.shifts.dtos.clients;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private Boolean selfResponsible;
    private String responsibleContactName;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String notes;
}
