package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.profile.UpdateProfilePhotoRequest;
import com.barbershop.shifts.dtos.profile.UserProfileResponse;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.UserRepository;
import com.barbershop.shifts.services.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private static final int MAX_PROFILE_IMAGE_LENGTH = 900_000;

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public UserProfileController(CurrentUserService currentUserService, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserProfileResponse getProfile() {
        return toResponse(currentUserService.getCurrentUser());
    }

    @PutMapping("/photo")
    public UserProfileResponse updateProfilePhoto(@RequestBody UpdateProfilePhotoRequest request) {
        User user = currentUserService.getCurrentUser();
        user.setProfileImageUrl(normalizeProfileImage(request.getProfileImageUrl()));
        return toResponse(userRepository.save(user));
    }

    private String normalizeProfileImage(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.trim().isEmpty()) {
            return null;
        }

        String normalized = profileImageUrl.trim();
        if (normalized.length() > MAX_PROFILE_IMAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile image is too large");
        }

        if (!normalized.matches("^data:image/(png|jpeg|jpg|webp);base64,.+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid profile image");
        }

        return normalized;
    }

    private UserProfileResponse toResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setBarbershopName(user.getBarbershop() == null ? null : user.getBarbershop().getName());
        response.setProfileImageUrl(user.getProfileImageUrl());
        return response;
    }
}
