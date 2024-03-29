package com.github.storytime.scheduler;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.service.async.UserAsyncService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.CacheNames.*;
import static com.github.storytime.config.props.ZenDataConstants.INITIAL_TIMESTAMP;
import static com.github.storytime.error.AsyncErrorHandlerUtil.logCache;
import static com.github.storytime.service.util.STUtils.createSt;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CacheResetScheduler {

    private static final Logger LOGGER = getLogger(CacheResetScheduler.class);
    private final ZenAsyncService zenAsyncService;
    private final UserAsyncService userAsyncService;

    @Autowired
    public CacheResetScheduler(final ZenAsyncService zenAsyncService,
                               final UserAsyncService userAsyncService) {
        this.zenAsyncService = zenAsyncService;
        this.userAsyncService = userAsyncService;
    }

    @Scheduled(fixedRateString = "${cache.clean.currency.millis}", initialDelayString = "${cache.clean.currency.millis}")
    @CacheEvict(allEntries = true, value = {CURRENCY_CACHE})
    public void cleaningCurrencyCache() {
        LOGGER.debug("Cleaning up currency cache ...");
    }

    @Scheduled(fixedRateString = "${cache.clean.zentags.millis}", initialDelayString = "${cache.clean.zentags.millis}")
    @CacheEvict(allEntries = true, beforeInvocation = true, value = {TR_TAGS_DIFF, OUT_DATA_BY_MONTH, IN_DATA_BY_MONTH,
            OUT_DATA_BY_YEAR, IN_DATA_BY_YEAR, OUT_DATA_BY_QUARTER, IN_DATA_BY_QUARTER, ZM_SAVING_CACHE
    })
    public void cleaningZenDiffTagsCache() {
        LOGGER.debug("Cleaning up tags cache ...");
        userAsyncService
                .getAllUsers()
                .thenAccept(usersList -> {
                    final var st = createSt();
                    final var completableFutures = usersList.stream()
                            .filter(AppUser::isEnabled)
                            .map(user -> zenAsyncService.zenDiffByUserTagsAndTransaction(user, INITIAL_TIMESTAMP))
                            .toList();
                    allOf(completableFutures.toArray(new CompletableFuture[0]))
                            .whenComplete((r, e) -> logCache(st, LOGGER, e));
                });
    }
}
