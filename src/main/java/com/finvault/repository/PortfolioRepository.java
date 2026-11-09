package com.finvault.repository;

import com.finvault.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    List<Portfolio> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Portfolio> findByUserIdAndIsDefaultTrue(UUID userId);
    Optional<Portfolio> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);

    @Query("SELECT SUM(p.currentValue) FROM Portfolio p WHERE p.user.id = :userId")
    java.math.BigDecimal getTotalValueByUserId(UUID userId);
}
