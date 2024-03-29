package com.github.storytime.service.pb;

import com.github.storytime.builder.PbRequestBuilder;
import com.github.storytime.config.CustomConfig;
import com.github.storytime.error.exception.PbSignatureException;
import com.github.storytime.mapper.response.PbResponseMapper;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.async.PbAsyncService;
import com.github.storytime.service.misc.AdditionalCommentService;
import com.github.storytime.service.misc.DateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.github.storytime.config.props.Constants.CARD_LAST_DIGITS;
import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.error.AsyncErrorHandlerUtil.logPbCf;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.right;

@Service
public class PbStatementsService {

    private static final Logger LOGGER = LogManager.getLogger(PbStatementsService.class);

    private final DateService dateService;
    private final AdditionalCommentService additionalCommentService;
    private final CustomConfig customConfig;
    private final PbRequestBuilder pbRequestBuilder;
    private final PbResponseMapper pbResponseMapper;
    private final PbAsyncService pbAsyncService;

    @Autowired
    public PbStatementsService(
            final CustomConfig customConfig,
            final PbResponseMapper pbResponseMapper,
            final PbRequestBuilder statementRequestBuilder,
            final AdditionalCommentService additionalCommentService,
            final DateService dateService,
            final PbAsyncService pbAsyncService) {
        this.customConfig = customConfig;
        this.pbResponseMapper = pbResponseMapper;
        this.pbRequestBuilder = statementRequestBuilder;
        this.additionalCommentService = additionalCommentService;
        this.dateService = dateService;
        this.pbAsyncService = pbAsyncService;
    }

    public CompletableFuture<List<Statement>> getAwsPbTransactions(final AppUser appUser,
                                                                   final PbMerchant merchantInfo,
                                                                   final ZonedDateTime startDate,
                                                                   final ZonedDateTime endDate) {

        logValues(appUser, merchantInfo, startDate, endDate);
        final var requestToBank = pbRequestBuilder.buildStatementRequest(merchantInfo, dateService.toPbFormat(startDate), dateService.toPbFormat(endDate));
        return pbAsyncService.pullPbTransactions(requestToBank, merchantInfo.getShortDesc())
                .thenApply(Optional::get)
                .thenApply(responseFromBank -> handleResponse(appUser, merchantInfo, startDate, endDate, responseFromBank))
                .thenApply(stList -> additionalCommentService.addAdditionalAwsComments(stList, merchantInfo, appUser.getTimeZone()))
                .whenComplete((r, e) -> logPbCf(appUser.getId(), LOGGER, e));
    }

    private List<Statement> handleResponse(final AppUser u,
                                           final PbMerchant m,
                                           final ZonedDateTime startDate,
                                           final ZonedDateTime endDate,
                                           final ResponseEntity<String> body) {
        try {
            final List<Statement> allPbTransactions = pbResponseMapper.mapStatementRequestBody(body, m.getShortDesc());
            final List<Statement> onlyNewPbTransactions = filterNewPbTransactions(startDate, endDate, allPbTransactions, u);
            m.setSyncStartDate(endDate.toInstant().toEpochMilli()); // later will do save to update last sync time
            return onlyNewPbTransactions;
        } catch (PbSignatureException e) {
            // roll back for 1 hrs
            final var rollBackStartDateMillis = startDate.minusHours(customConfig.getPbRollBackPeriod()).toInstant().toEpochMilli();
            final var mDesc = ofNullable(m.getShortDesc()).orElse(EMPTY);
            final var mId = m.getMerchantId();
            final var sDate = dateService.millisToIsoFormat(startDate);
            final var rollBackTime = dateService.millisToIsoFormat(rollBackStartDateMillis, u);

            LOGGER.error("Desc: [{}] mId: [{}] invalid signature, rollback from: [{}] to: [{}]", mDesc, mId, sDate, rollBackTime);

            final var now = dateService.getUserStarDateInMillis(u);
            if (rollBackStartDateMillis > (now - customConfig.getMaxRollbackPeriod())) {
                m.setSyncStartDate(rollBackStartDateMillis);
            } else {
                LOGGER.error("Desc: [{}] mId: [{}] invalid signature, failed to rollback from: [{}] to: [{}] date is too big", mDesc, mId, sDate, rollBackTime);
            }
            return emptyList();
        }
    }


    public List<Statement> filterNewPbTransactions(final ZonedDateTime start,
                                                   final ZonedDateTime end,
                                                   final List<Statement> pbStatements,
                                                   final AppUser appUser) {
        final Comparator<ZonedDateTime> comparator = comparing(zdt -> zdt.truncatedTo(MILLIS));
        // sometimes new transactions can be available with delay, so we need to change start time of filtering
        final ZonedDateTime searchStartTime = start.minus(customConfig.getFilterTimeMillis(), MILLIS);
        return pbStatements
                .stream()
                .filter(getStatementComparatorPredicate(end, appUser, comparator, searchStartTime))
                .toList();
    }

    public Predicate<Statement> getStatementComparatorPredicate(final ZonedDateTime end,
                                                                final AppUser appUser,
                                                                final Comparator<ZonedDateTime> comparator,
                                                                final ZonedDateTime searchStartTime) {
        return t -> {
            final ZonedDateTime tTime = dateService.xmlDateTimeToZoned(t.getTrandate(), t.getTrantime(), appUser.getTimeZone());
            return comparator.compare(searchStartTime, tTime) <= 0 && comparator.compare(end, tTime) > 0;
        };
    }

    private void logValues(final AppUser appUser, final PbMerchant merchantInfo, final ZonedDateTime startDate, final ZonedDateTime endDate) {
        final var sd = dateService.millisToIsoFormat(startDate);
        final var userId = appUser.getId();
        final var desc = ofNullable(merchantInfo.getShortDesc()).orElse(EMPTY);
        final var id = merchantInfo.getId();
        final var merchantId = merchantInfo.getMerchantId();
        final var ed = dateService.millisToIsoFormat(endDate);
        final var lastDigits = right(merchantInfo.getCardNumber(), CARD_LAST_DIGITS);
        LOGGER.info("Syncing user: [{}], desc: [{}], mId: [{}], mNumb: [{}], sd: [{}] lastSync/ed: [{}], card: [{}]", userId, desc, id, merchantId, sd, ed, lastDigits);
    }
}
