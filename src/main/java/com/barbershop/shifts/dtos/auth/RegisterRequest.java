package com.barbershop.shifts.dtos.auth;

public record RegisterRequest(
        String barbershopName,
        String adminName,
        String branchName,
        String branchAddress,
        String email,
        String password
) {
}
