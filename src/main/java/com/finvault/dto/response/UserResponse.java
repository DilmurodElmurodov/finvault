package com.finvault.dto.response;

import com.finvault.entity.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String username;
    private String fullName;
    private String profilePicture;
    private Boolean emailVerified;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
