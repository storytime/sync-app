package com.github.storytime.mapper.pb;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.TransactionItem;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingLong;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbToZenDataMapper {

    private static final Logger LOGGER = getLogger(PbToZenDataMapper.class);
    private final PbToZenAccountMapper pbToZenAccountMapper;
    private final PbToZenTransactionMapper pbToZenTransactionMapper;

    @Autowired
    public PbToZenDataMapper(final PbToZenAccountMapper pbToZenAccountMapper,
                             final PbToZenTransactionMapper pbToZenTransactionMapper) {

        this.pbToZenAccountMapper = pbToZenAccountMapper;
        this.pbToZenTransactionMapper = pbToZenTransactionMapper;
    }

    public Optional<ZenDiffRequest> buildZenReqFromPbData(final List<List<Statement>> newPbTransaction,
                                                          final ZenResponse zenDiff,
                                                          final AppUser appUser) {

        try {
            LOGGER.debug("Starting build data for zen, for user: [{}] ", appUser.getId());
            final boolean isAccountsPushNeeded = newPbTransaction
                    .stream()
                    .map(t -> pbToZenAccountMapper.mapPbAccountToZen(t, zenDiff))
                    .collect(toSet())
                    .stream()
                    .anyMatch(r -> r == TRUE);

            LOGGER.debug("No new accounts, from bank, for user: [{}]", appUser.getId());
            final List<TransactionItem> allTransactionsToZen = newPbTransaction
                    .stream()
                    .map(t -> pbToZenTransactionMapper.mapPbTransactionToZen(t, zenDiff, appUser))
                    .flatMap(Collection::stream)
                    .sorted(comparingLong(TransactionItem::getCreated).reversed()).toList();

            LOGGER.debug("Transactions from bank, are ready for user: [{}]", appUser.getId());
            return of(new ZenDiffRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setLastServerTimestamp(zenDiff.getServerTimestamp())
                    .setAccount(isAccountsPushNeeded ? zenDiff.getAccount() : emptyList())
                    .setTransaction(allTransactionsToZen));

        } catch (Exception e) {
            LOGGER.debug("Error", e);
            return empty();
        }
    }
}