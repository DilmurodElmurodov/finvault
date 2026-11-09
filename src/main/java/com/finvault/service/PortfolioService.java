package com.finvault.service;

import com.finvault.dto.request.PortfolioRequest;
import com.finvault.entity.Portfolio;
import java.util.List;
import java.util.UUID;

public interface PortfolioService {
    List<Portfolio> getUserPortfolios(UUID userId);
    Portfolio createPortfolio(UUID userId, PortfolioRequest request);
    Portfolio updatePortfolio(UUID userId, UUID portfolioId, PortfolioRequest request);
    void deletePortfolio(UUID userId, UUID portfolioId);
    Portfolio getPortfolioById(UUID userId, UUID portfolioId);
}
