package com.barbershop.shifts.dtos.clients;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientRequest {

    @NotNull
    private Long id;

    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    private String firstName;

    private String lastName;

    private String documentNumber;

    private String notes;
}
