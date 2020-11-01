package com.github.storytime.service.http;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.currency.pb.cash.CashResponse;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class CurrencyHttpService {

    private static final Logger LOGGER = getLogger(CurrencyHttpService.class);
    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;

    @Autowired
    public CurrencyHttpService(final RestTemplate restTemplate, final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
    }

    public Optional<List<CashResponse>> pullPbCashRate() {
        try {
            LOGGER.info("Pulling PB Cash currency");
            final ResponseEntity<CashResponse[]> forEntity = restTemplate.getForEntity(customConfig.getPbCashUrl(), CashResponse[].class);
            return Optional.of(List.of(ofNullable(forEntity.getBody()).orElse(new CashResponse[]{})));
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol PB cash rate reason:[{}]", e.getMessage());
            return empty();
        }
    }
}