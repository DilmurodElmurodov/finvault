package com.finvault.repository;

import com.finvault.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByPortfolioId(UUID portfolioId);
    Optional<Asset> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
    Optional<Asset> findByIdAndPortfolioUserId(UUID id, UUID userId);
    Page<Asset> findByPortfolioUserId(UUID userId, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.portfolio.user.id = :userId ORDER BY a.currentPrice * a.quantity DESC")
    List<Asset> findTopHoldingsByUserId(UUID userId, Pageable pageable);
}
