package com.github.storytime.scheduler;

import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.github.storytime.config.props.CacheNames.*;
import static com.github.storytime.config.props.Constants.INITIAL_TIMESTAMP;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CacheResetScheduler {

    private static final Logger LOGGER = getLogger(CacheResetScheduler.class);
    private final ZenAsyncService zenAsyncService;
    private final UserService userService;

    @Autowired
    public CacheResetScheduler(final ZenAsyncService zenAsyncService,
                               final UserService userMsAsyncService) {
        this.zenAsyncService = zenAsyncService;
        this.userService = userMsAsyncService;
    }

    @Scheduled(fixedRateString = "${cache.clean.currency.millis}")
    @CacheEvict(allEntries = true, value = {CURRENCY_CACHE})
    public void cleaningCurrencyCache() {
        LOGGER.debug("Cleaning up currency cache ...");
    }

    @Scheduled(fixedRateString = "45000")
    @CacheEvict(allEntries = true, value = {TR_TAGS_DIFF, OUT_DATA_BY_MONTH, IN_DATA_BY_MONTH,
            OUT_DATA_BY_YEAR, IN_DATA_BY_YEAR, OUT_DATA_BY_QUARTER, IN_DATA_BY_QUARTER})
    public void cleaningZenDiffTagsCache() {
        LOGGER.debug("Cleaning up tags cache ...");
        userService
                .findAllAsync()
                .thenAccept(usersList -> usersList.forEach(user -> zenAsyncService.zenDiffByUserTagsAndTransaction(user, INITIAL_TIMESTAMP)))
                .thenAccept(x -> LOGGER.debug("Warming up, tags cache ..."));

    }

    @Scheduled(fixedRateString = "${cache.clean.payee.millis}")
    @CacheEvict(allEntries = true, value = {CUSTOM_PAYEE})
    public void cleaningCustomPayeeCache() {
        LOGGER.debug("Cleaning up custom payee cache ...");
    }
}
