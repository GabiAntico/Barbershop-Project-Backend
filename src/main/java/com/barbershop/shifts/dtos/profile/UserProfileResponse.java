package com.barbershop.shifts.dtos.profile;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String displayName;
    private String email;
    private String role;
    private String barbershopName;
    private String profileImageUrl;
}
