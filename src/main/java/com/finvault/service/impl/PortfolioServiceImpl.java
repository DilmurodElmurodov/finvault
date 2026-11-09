package com.finvault.service.impl;

import com.finvault.dto.request.PortfolioRequest;
import com.finvault.entity.Portfolio;
import com.finvault.entity.User;
import com.finvault.exception.ResourceNotFoundException;
import com.finvault.repository.PortfolioRepository;
import com.finvault.repository.UserRepository;
import com.finvault.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Override @Transactional(readOnly = true)
    public List<Portfolio> getUserPortfolios(UUID userId) {
        return portfolioRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Portfolio createPortfolio(UUID userId, PortfolioRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // If this is set as default, unset any existing default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            portfolioRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(p -> { p.setIsDefault(false); portfolioRepository.save(p); });
        }

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .initialInvestment(request.getInitialInvestment())
                .type(request.getType() != null ? request.getType() : Portfolio.PortfolioType.MIXED)
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();

        return portfolioRepository.save(portfolio);
    }

    @Override
    public Portfolio updatePortfolio(UUID userId, UUID portfolioId, PortfolioRequest request) {
        Portfolio portfolio = getPortfolioById(userId, portfolioId);
        if (request.getName() != null) portfolio.setName(request.getName());
        if (request.getDescription() != null) portfolio.setDescription(request.getDescription());
        if (request.getType() != null) portfolio.setType(request.getType());
        return portfolioRepository.save(portfolio);
    }

    @Override
    public void deletePortfolio(UUID userId, UUID portfolioId) {
        Portfolio portfolio = getPortfolioById(userId, portfolioId);
        portfolioRepository.delete(portfolio);
        log.info("Portfolio deleted: {} by user: {}", portfolioId, userId);
    }

    @Override @Transactional(readOnly = true)
    public Portfolio getPortfolioById(UUID userId, UUID portfolioId) {
        return portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));
    }
}
