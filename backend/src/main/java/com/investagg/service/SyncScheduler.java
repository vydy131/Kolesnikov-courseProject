package com.investagg.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final SyncService syncService;

    // Every 15 minutes
    @Scheduled(fixedDelay = 900_000, initialDelay = 60_000)
    public void scheduledSync() {
        log.info("Starting scheduled broker sync...");
        syncService.syncAllActiveAccounts();
    }
}
