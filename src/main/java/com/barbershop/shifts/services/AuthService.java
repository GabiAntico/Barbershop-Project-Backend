package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.auth.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    void register(RegisterRequest request);
    void changePassword(String currentPassword, String newPassword);
}
