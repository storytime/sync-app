package com.github.storytime.function;

import com.github.storytime.model.aws.AwsCurrencyRates;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.Optional.empty;

@Deprecated
public class FunctionUtils {

    public static Supplier<Optional<? extends AwsCurrencyRates>> logAndGetEmpty(final Logger logger,
                                                                                final Level level,
                                                                                final String msg) {
        return () -> {
            logger.log(level, msg);
            return empty();
        };
    }

    public static Supplier<? extends Optional<? extends Optional<CompletableFuture<Void>>>> logAndGetEmptyForSync(final Logger logger,
                                                                                                                  final Level level,
                                                                                                                  final String msg) {
        return () -> {
            logger.log(level, msg);
            return empty();
        };
    }
}
