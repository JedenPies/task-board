package net.patrykdobrowolski.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.patrykdobrowolski.auth.db.repository.SpringDataUserTokensRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final SpringDataUserTokensRepository userTokensRepository;
    private final Clock clock;

    @Scheduled(cron = "${spring.token-cleanup-cron}")
    @Transactional
    public void removeExpiredOrRevokedTokens() {
        log.info("Removing expired or revoked tokens");
        userTokensRepository.deleteByValidUntilBeforeOrIsRevokedTrue(clock.instant());
        log.info("Expired or revoked tokens removed");
    }
}
