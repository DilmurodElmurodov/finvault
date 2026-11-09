package com.finvault.controller;

import com.finvault.dto.request.PortfolioRequest;
import com.finvault.dto.response.ApiResponse;
import com.finvault.entity.Portfolio;
import com.finvault.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.finvault.repository.UserRepository;
import com.finvault.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Portfolio Controller - CRUD operations for investment portfolios
 */
@RestController
@RequestMapping("/portfolios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Portfolio", description = "Investment portfolio management")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()))
                .getId();
    }

    @GetMapping
    @Operation(summary = "Get all user portfolios")
    public ResponseEntity<ApiResponse<List<Portfolio>>> getUserPortfolios(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getUserPortfolios(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get portfolio by ID")
    public ResponseEntity<ApiResponse<Portfolio>> getPortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getPortfolioById(userId, id)));
    }

    @PostMapping
    @Operation(summary = "Create new portfolio")
    public ResponseEntity<ApiResponse<Portfolio>> createPortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PortfolioRequest request) {
        UUID userId = getCurrentUserId(userDetails);
        Portfolio portfolio = portfolioService.createPortfolio(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Portfolio created", portfolio));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update portfolio")
    public ResponseEntity<ApiResponse<Portfolio>> updatePortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody PortfolioRequest request) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Portfolio updated",
                portfolioService.updatePortfolio(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete portfolio")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = getCurrentUserId(userDetails);
        portfolioService.deletePortfolio(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Portfolio deleted", null));
    }
}
