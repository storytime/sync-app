package com.github.storytime.function;

import com.github.storytime.mapper.PbStatementsAlreadyPushedUtil;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.aws.PbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.async.StatementAsyncService;
import com.github.storytime.service.misc.DateService;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.of;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbSyncLambdaHolder {

    private static final Logger LOGGER = getLogger(PbSyncLambdaHolder.class);

    public BiFunction<AppUser, PbMerchant, ZonedDateTime> getAwsStartDate(final DateService dateService) {
        return (user, merchant) -> dateService.millisAwsUserDate(merchant.getSyncStartDate(), user);
    }

    public TrioFunction<AppUser, PbMerchant, ZonedDateTime, ZonedDateTime> getAwsEndDate(final Instant nowInst) {
        return (appUser, merchantInfo, startDate) -> {
            final var period = ofMillis(merchantInfo.getSyncPeriod());
            final var now = nowInst.atZone(of(appUser.getTimeZone()));
            return between(startDate, now).toMillis() < merchantInfo.getSyncPeriod() ? now : startDate.plus(period);
        };
    }

    public BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<PbStatement>>> onAwsDbRegularSyncSuccess(final StatementAsyncService statementAsyncService) {

        return (pushedByNotCached, userId) -> statementAsyncService.getAllStatementsByUser(userId)
                .thenCompose(dfStatements -> {
                            final Set<String> pushedByNotCachedMapped = pushedByNotCached
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .collect(toUnmodifiableSet())
                                    .stream()
                                    .map(PbStatementsAlreadyPushedUtil::generateUniqString)
                                    .collect(toUnmodifiableSet());

                            final Set<String> dfStatementsOrEmpty = ofNullable(dfStatements.getAlreadyPushed()).orElse(emptySet());
                            final Set<String> allNewStatements = concat(pushedByNotCachedMapped.stream(), dfStatementsOrEmpty.stream())
                                    .collect(toUnmodifiableSet());

                            LOGGER.debug("Update already, df: [{}], pushed: [{}], all to push: [{}]", pushedByNotCachedMapped.size(), dfStatementsOrEmpty.size(), allNewStatements.size());

                            return statementAsyncService.saveAll(dfStatements.setAlreadyPushed(allNewStatements), userId);
                        }
                );
    }
}
