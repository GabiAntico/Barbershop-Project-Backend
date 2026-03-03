package com.barbershop.shifts.dtos.clients;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreationClientRequest {

    @NotNull
    @NotBlank
    @Email
    private String email;

    private String firstName;

    private String lastName;

    private String documentNumber;
}
