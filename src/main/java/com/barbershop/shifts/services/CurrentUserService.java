package com.barbershop.shifts.services;

import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.BranchRepositoryJpa;
import com.barbershop.shifts.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Comparator;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final BranchRepositoryJpa branchRepository;
    private final HttpServletRequest request;

    public CurrentUserService(
            UserRepository userRepository,
            BranchRepositoryJpa branchRepository,
            HttpServletRequest request
    ) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.request = request;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public Branch getCurrentBranch() {
        User user = getCurrentUser();
        String branchIdHeader = request.getHeader("X-Branch-Id");

        if (branchIdHeader != null && !branchIdHeader.isBlank()) {
            Long branchId;
            try {
                branchId = Long.valueOf(branchIdHeader);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid branch context");
            }

            Branch branch = branchRepository.findByIdAndBarbershop(branchId, user.getBarbershop())
                    .orElse(null);

            if (branch == null) {
                return getFallbackBranch(user);
            }

            if (!isAdmin(user) && user.getBranches().stream().noneMatch(assigned -> assigned.getId().equals(branch.getId()))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Branch not assigned to user");
            }

            return branch;
        }

        return getFallbackBranch(user);
    }

    private Branch getFallbackBranch(User user) {
        return user.getBranches().stream()
                .min(Comparator.comparing(Branch::getId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No branch context available"));
    }

    public boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
