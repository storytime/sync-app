package com.github.storytime.mapper;

import com.github.storytime.model.zen.InstrumentItem;
import com.github.storytime.model.zen.ZenResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.github.storytime.config.props.Constants.DEFAULT_CURRENCY_ZEN;
import static com.github.storytime.config.props.Constants.SPACE_SEPARATOR;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.substringAfter;

@Component
public class ZenInstrumentsMapper {

    public Integer getZenCurrencyFromPbTransaction(final ZenResponse zenDiff, final String transactionString) {
        final String shortName = substringAfter(transactionString, SPACE_SEPARATOR);
        return ofNullable(zenDiff.getInstrument()).orElse(Collections.emptyList())
                .stream()
                .filter(zenCurr -> zenCurr.getShortTitle().equalsIgnoreCase(shortName))
                .findFirst()
                .map(InstrumentItem::getId)
                .orElse(DEFAULT_CURRENCY_ZEN);
    }

}
