package com.barbershop.shifts.dtos.auth;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}
