package com.investagg.repository;

import com.investagg.entity.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, UUID> {
    List<InvestmentAccount> findByUserIdAndDeletedAtIsNull(UUID userId);
    Optional<InvestmentAccount> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
    boolean existsByUserIdAndBrokerIdAndAccountNumberAndDeletedAtIsNull(UUID userId, UUID brokerId, String accountNumber);
}
