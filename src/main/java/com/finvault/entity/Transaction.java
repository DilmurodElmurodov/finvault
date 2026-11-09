package com.finvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction Entity - Financial transaction record
 */
@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Column(precision = 18, scale = 6)
    private BigDecimal fee;

    @Column(length = 10)
    private String currency;

    @Column(length = 500)
    private String notes;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        BUY, SELL, DIVIDEND, DEPOSIT, WITHDRAWAL, FEE
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = price.multiply(quantity);
        if (fee != null) total = total.add(fee);
        return total;
    }
}
