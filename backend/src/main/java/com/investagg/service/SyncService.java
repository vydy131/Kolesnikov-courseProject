package com.investagg.service;

import com.investagg.client.BrokerClient;
import com.investagg.client.BrokerPosition;
import com.investagg.client.BrokerTransaction;
import com.investagg.entity.Asset;
import com.investagg.entity.InvestmentAccount;
import com.investagg.entity.Portfolio;
import com.investagg.entity.Transaction;
import com.investagg.entity.enums.AccountStatus;
import com.investagg.entity.enums.NotificationType;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.exception.ForbiddenException;
import com.investagg.repository.AssetRepository;
import com.investagg.repository.InvestmentAccountRepository;
import com.investagg.repository.PortfolioRepository;
import com.investagg.repository.TransactionRepository;
import com.investagg.security.AesEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SyncService {

    private final InvestmentAccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final BrokerClient brokerClient;
    private final AesEncryptionService aesService;
    private final NotificationService notificationService;

    @Transactional
    public void syncBrokerAccount(UUID accountId) {
        InvestmentAccount account = accountRepository.findById(accountId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));

        log.info("Syncing account {} (broker={})", accountId, account.getBroker().getName());

        try {
            // Decrypt token for API call
            String plainToken = aesService.decrypt(account.getBrokerToken());
            account.setBrokerToken(plainToken); // temporary for client call

            reconcilePositions(account);
            importTransactions(account);

            account.setBrokerToken(aesService.encrypt(plainToken)); // re-encrypt
            account.setSyncedAt(OffsetDateTime.now());
            account.setAccountStatus(AccountStatus.ACTIVE);
            accountRepository.save(account);

            log.info("Sync completed for account {}", accountId);

        } catch (Exception ex) {
            log.error("Sync failed for account {}: {}", accountId, ex.getMessage(), ex);
            account.setAccountStatus(AccountStatus.ERROR);
            accountRepository.save(account);

            notificationService.send(
                    account.getUser().getId(),
                    NotificationType.SYNC_ERROR,
                    "Sync error",
                    "Failed to sync account " + account.getAccountNumber() + ": " + ex.getMessage()
            );
        }
    }

    private void reconcilePositions(InvestmentAccount account) {
        List<BrokerPosition> positions = brokerClient.fetchPositions(account);
        Portfolio portfolio = portfolioRepository.findByUserId(account.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));

        for (BrokerPosition pos : positions) {
            Optional<Asset> existing = assetRepository.findByPortfolioIdAndTicker(portfolio.getId(), pos.ticker());
            Asset asset = existing.orElseGet(() -> {
                Asset a = new Asset();
                a.setPortfolio(portfolio);
                a.setTicker(pos.ticker());
                return a;
            });
            asset.setName(pos.name());
            asset.setQty(pos.qty());
            asset.setAvgPrice(pos.avgPrice());
            asset.setCurrency(pos.currency());
            asset.setUpdatedAt(OffsetDateTime.now());
            assetRepository.save(asset);
        }
    }

    private void importTransactions(InvestmentAccount account) {
        LocalDate from = account.getSyncedAt() != null
                ? account.getSyncedAt().toLocalDate()
                : LocalDate.now().minusDays(30);

        List<BrokerTransaction> brokerTxns = brokerClient.fetchTransactions(account, from);

        for (BrokerTransaction bt : brokerTxns) {
            Transaction tx = new Transaction();
            tx.setAccount(account);
            tx.setTransactionType(bt.type());
            tx.setAmount(bt.amount());
            tx.setCurrency(bt.currency());
            tx.setOccurredAt(bt.occurredAt());
            transactionRepository.save(tx);
        }
    }

    @Transactional
    public void syncAllActiveAccounts() {
        List<InvestmentAccount> accounts =
                accountRepository.findByDeletedAtIsNullAndAccountStatusNot(AccountStatus.REVOKED);

        log.info("Scheduled sync: processing {} accounts", accounts.size());
        accounts.forEach(a -> syncBrokerAccount(a.getId()));
    }
}
