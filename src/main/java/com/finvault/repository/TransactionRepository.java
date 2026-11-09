package com.finvault.repository;

import com.finvault.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT SUM(t.price * t.quantity) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'BUY'")
    BigDecimal getTotalBuyAmountByUserId(UUID userId);

    @Query("SELECT SUM(t.price * t.quantity) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'SELL'")
    BigDecimal getTotalSellAmountByUserId(UUID userId);

    List<Transaction> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime from, LocalDateTime to);
    long countByUserId(UUID userId);
}
