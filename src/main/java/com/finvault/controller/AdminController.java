package com.finvault.controller;

import com.finvault.dto.response.ApiResponse;
import com.finvault.entity.User;
import com.finvault.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Controller - Platform administration (ADMIN role only)
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Panel", description = "Administrative endpoints - Admin role required")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy)));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/users/{userId}/toggle-active")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<ApiResponse<Void>> toggleUserActive(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.finvault.exception.ResourceNotFoundException("User", "id", userId));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(
                "User " + (user.getIsActive() ? "activated" : "deactivated"), null));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable UUID userId, @RequestParam User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.finvault.exception.ResourceNotFoundException("User", "id", userId));
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Role updated to " + role, null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform analytics dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformStats() {
        long totalUsers = userRepository.count();
        long newUsersThisMonth = userRepository.countNewUsersSince(
                LocalDateTime.now().withDayOfMonth(1).withHour(0));

        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "totalUsers", totalUsers,
            "newUsersThisMonth", newUsersThisMonth,
            "platform", "FinVault",
            "version", "1.0.0"
        )));
    }
}
