package com.barbershop.shifts.dtos.auth;

public record RegisterRequest(
        String email,
        String password
) {
}
