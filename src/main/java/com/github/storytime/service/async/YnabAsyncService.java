package com.github.storytime.service.async;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.ynab.account.YnabAccountResponse;
import com.github.storytime.model.ynab.budget.YnabBudgetResponse;
import com.github.storytime.model.ynab.category.YnabCategoryResponse;
import com.github.storytime.model.ynab.transaction.from.TransactionsFormYnab;
import com.github.storytime.model.ynab.transaction.request.YnabTransactionsRequest;
import com.github.storytime.service.http.YnabHttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class YnabAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(YnabAsyncService.class);

    private final Executor cfThreadPool;
    private final YnabHttpService ynabHttpService;

    @Autowired
    public YnabAsyncService(final YnabHttpService ynabHttpService,
                            final Executor cfThreadPool) {
        this.ynabHttpService = ynabHttpService;
        this.cfThreadPool = cfThreadPool;
    }

    public CompletableFuture<Optional<YnabCategoryResponse>> getYnabCategories(final AppUser user,
                                                                               final String budgetToSync) {
        return supplyAsync(() -> ynabHttpService.getCategories(user, budgetToSync), cfThreadPool);
    }

    public CompletableFuture<Optional<YnabAccountResponse>> getYnabAccounts(final AppUser appUser,
                                                                            final String budgetToSync) {
        LOGGER.debug("Fetching Ynab accounts, for user: [{}]", appUser.getId());
        return supplyAsync(() -> ynabHttpService.getAccounts(appUser, budgetToSync), cfThreadPool);
    }

    public CompletableFuture<Optional<TransactionsFormYnab>> getYnabTransactions(final AppUser appUser,
                                                                                 final String budgetToReconcile) {
        LOGGER.debug("Fetching Ynab transactions, for user: [{}]", appUser.getId());
        return supplyAsync(() -> ynabHttpService.getYnabTransactions(appUser, budgetToReconcile), cfThreadPool);
    }

    public CompletableFuture<Optional<YnabBudgetResponse>> getYnabBudget(final AppUser appUser) {
        LOGGER.debug("Fetching Ynab budgets, for user: [{}]", appUser.getId());
        return supplyAsync(() -> ynabHttpService.getBudget(appUser), cfThreadPool);
    }

    public CompletableFuture<Optional<String>> pushToYnab(final AppUser appUser,
                                                          final String id,
                                                          final YnabTransactionsRequest request) {
        LOGGER.debug("Pushing  zen tr to Ynab, for user: [{}], budget:[{}], tr count [{}]", appUser.getId(), id, request.getTransactions().size());
        request.getTransactions()
                .forEach(yTr -> LOGGER.debug("Going to push to YNAB: [{}], payee: [{}], date: [{}], catI: [{}]", yTr.getAmount(), yTr.getPayeeName(), yTr.getDate(), yTr.getCategoryId()));

        return supplyAsync(() -> ynabHttpService.pushToYnab(appUser, id, request), cfThreadPool);
    }
}