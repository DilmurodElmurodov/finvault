package com.finvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Asset Entity - Individual investment holding
 */
@Entity
@Table(name = "assets", indexes = {
    @Index(name = "idx_asset_symbol", columnList = "symbol"),
    @Index(name = "idx_asset_portfolio", columnList = "portfolio_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", length = 20)
    private AssetType assetType;

    @Column(precision = 18, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(name = "average_buy_price", precision = 18, scale = 6, nullable = false)
    private BigDecimal averageBuyPrice;

    @Column(name = "current_price", precision = 18, scale = 6)
    private BigDecimal currentPrice;

    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AssetType {
        STOCK, CRYPTO, ETF, BOND, FOREX, COMMODITY
    }

    // Calculated fields
    public BigDecimal getCurrentValue() {
        if (currentPrice == null || quantity == null) return BigDecimal.ZERO;
        return quantity.multiply(currentPrice);
    }

    public BigDecimal getProfitLoss() {
        if (currentPrice == null || quantity == null || averageBuyPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(currentPrice.subtract(averageBuyPrice));
    }

    public BigDecimal getProfitLossPercent() {
        if (averageBuyPrice == null || averageBuyPrice.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return currentPrice.subtract(averageBuyPrice)
                .divide(averageBuyPrice, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
