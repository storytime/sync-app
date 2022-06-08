package com.github.storytime.service.async;

import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.http.ZenDiffHttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.CacheNames.TR_TAGS_DIFF;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class ZenAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(ZenAsyncService.class);

    private final Executor pool;
    private final ZenDiffHttpService zenDiffHttpService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;

    @Autowired
    public ZenAsyncService(final ZenDiffHttpService zenDiffHttpService,
                           final ZenDiffLambdaHolder zenDiffLambdaHolder,
                           final Executor cfThreadPool) {
        this.zenDiffHttpService = zenDiffHttpService;
        this.pool = cfThreadPool;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForPb(final AwsUser appUser) {
        LOGGER.debug("Fetching all ZEN data for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getInitialFunction(appUser)), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForSavings(final AwsUser appUser) {
        LOGGER.debug("Fetching ZEN accounts/instruments for savings for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getSavingsFunction(appUser)), pool);
    }

    @Cacheable(cacheNames = TR_TAGS_DIFF, key = "#appUser.getId")
    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserTagsAndTransaction(final AwsUser appUser, long startDate) {
        LOGGER.debug("Fetching ZEN accounts/tags for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getAccountAndTags(appUser, startDate)), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForPbAccReconcile(final AwsUser appUser, long startDate) {
        LOGGER.debug("Fetching ZEN accounts for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getAccount(appUser, startDate)), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> pushToZen(final AwsUser appUser,
                                                              final ZenDiffRequest request) {
        LOGGER.debug("Pushing zen diff for user: [{}] - stared", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.pushToZen(appUser, request), pool);
    }
}
