package com.investagg.repository;

import com.investagg.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountIdAndOccurredAtBetweenAndDeletedAtIsNull(
            UUID accountId, OffsetDateTime from, OffsetDateTime to);
}
