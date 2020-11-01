package com.github.storytime.service;

import com.github.storytime.builder.PbRequestBuilder;
import com.github.storytime.mapper.response.PbAccountBalanceResponseMapper;
import com.github.storytime.mapper.response.PbResponseMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.PbAccountBalance;
import com.github.storytime.service.async.PbAsyncService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.right;

@Service
public class PbAccountService {

    private static final Logger LOGGER = LogManager.getLogger(PbAccountService.class);

    private final PbRequestBuilder pbRequestBuilder;
    private final PbAccountBalanceResponseMapper pbAccountBalanceResponseMapper;
    private final PbAsyncService pbAsyncService;
    private final PbResponseMapper pbResponseMapper;

    @Autowired
    public PbAccountService(final PbAccountBalanceResponseMapper pbAccountBalanceResponseMapper,
                            final PbAsyncService pbAsyncService,
                            final PbResponseMapper pbResponseMapper,
                            final PbRequestBuilder pbRequestBuilder) {
        this.pbAccountBalanceResponseMapper = pbAccountBalanceResponseMapper;
        this.pbAsyncService = pbAsyncService;
        this.pbRequestBuilder = pbRequestBuilder;
        this.pbResponseMapper = pbResponseMapper;
    }

    public CompletableFuture<List<PbAccountBalance>> getPbAsyncAccounts(final AppUser appUser,
                                                                        final List<MerchantInfo> merchantInfos) {

        LOGGER.debug("Fetching PB accounts, for user: [{}]", appUser.getId());

        final List<CompletableFuture<PbAccountBalance>> pbAccountCf = merchantInfos
                .stream()
                .map(merchantInfo -> getPbAsyncAccounts(appUser, merchantInfo))
                .collect(toUnmodifiableList());

        return CompletableFuture
                .allOf(pbAccountCf.toArray(new CompletableFuture[pbAccountCf.size()]))
                .thenApply(aVoid -> pbAccountCf.stream().map(CompletableFuture::join).collect(toUnmodifiableList()));
    }

    public CompletableFuture<PbAccountBalance> getPbAsyncAccounts(final AppUser u, final MerchantInfo m) {

        LOGGER.info("Fetching PB balance u:[{}] desc:[{}] mId:[{}] mNumb:[{}]", u.getId(), ofNullable(m.getShortDesc()).orElse(EMPTY), m.getId(), right(m.getCardNumber(), CARD_LAST_DIGITS));

        return pbAsyncService.pullPbAccounts(pbRequestBuilder.buildAccountRequest(m))
                .thenApply(r -> r.map(pbResponseMapper::mapAccountRequestBody).orElse(DEFAULT_ACC_BALANCE))
                .thenApply(r -> pbAccountBalanceResponseMapper.buildSimpleObject(r, m));
    }
}