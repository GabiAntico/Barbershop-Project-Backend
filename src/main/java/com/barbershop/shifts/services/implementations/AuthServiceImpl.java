package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.auth.RegisterRequest;
import com.barbershop.shifts.entities.Barbershop;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.BarbershopRepositoryJpa;
import com.barbershop.shifts.repositories.BranchRepositoryJpa;
import com.barbershop.shifts.repositories.UserRepository;
import com.barbershop.shifts.services.AuthService;
import com.barbershop.shifts.services.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BarbershopRepositoryJpa barbershopRepository;
    private final BranchRepositoryJpa branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public AuthServiceImpl(
            UserRepository userRepository,
            BarbershopRepositoryJpa barbershopRepository,
            BranchRepositoryJpa branchRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.barbershopRepository = barbershopRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    public void register(RegisterRequest request) {
        boolean exists = userRepository.findByEmail(request.email()).isPresent();

        if (exists) {
            throw new RuntimeException("El usuario ya existe");
        }

        Barbershop barbershop = new Barbershop();
        barbershop.setName(normalizeRequired(request.barbershopName(), "Barbershop name is required"));
        barbershop = barbershopRepository.save(barbershop);

        Branch branch = new Branch();
        branch.setName(normalizeRequired(request.branchName(), "Branch name is required"));
        branch.setAddress(normalize(request.branchAddress()));
        branch.setTimeZone(Branch.DEFAULT_TIME_ZONE);
        branch.setBarbershop(barbershop);
        branch = branchRepository.save(branch);

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ADMIN");
        user.setDisplayName(normalize(request.adminName()));
        user.setTemporaryPassword(false);
        user.setBarbershop(barbershop);
        user.getBranches().add(branch);

        userRepository.save(user);
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        User user = currentUserService.getCurrentUser();
        String normalizedCurrentPassword = normalizeRequired(currentPassword, "Current password is required");
        String normalizedNewPassword = normalizeRequired(newPassword, "New password is required");

        if (normalizedNewPassword.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must have at least 6 characters");
        }

        if (!passwordEncoder.matches(normalizedCurrentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(normalizedNewPassword));
        user.setTemporaryPassword(false);
        userRepository.save(user);
    }

    private String normalize(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return normalized;
    }
}
