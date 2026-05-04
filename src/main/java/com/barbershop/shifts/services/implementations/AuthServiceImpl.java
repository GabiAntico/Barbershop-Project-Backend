package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.auth.RegisterRequest;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.UserRepository;
import com.barbershop.shifts.services.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        boolean exists = userRepository.findByEmail(request.email()).isPresent();

        if (exists) {
            throw new RuntimeException("El usuario ya existe");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");

        userRepository.save(user);
    }
}
