package com.assignment.api.queue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PricingAPIQueueEntry extends APIQueuEntry {

    CompletableFuture<Map<String, BigDecimal>> countryCodePriceMapFuture;

    public PricingAPIQueueEntry(List<String> apiParameterList, CompletableFuture<Map<String, BigDecimal>> countryCodePriceMapFuture) {
        super(apiParameterList);
        this.countryCodePriceMapFuture = countryCodePriceMapFuture;
    }

    public CompletableFuture<Map<String, BigDecimal>> getCountryCodePriceMapFuture() {
        return countryCodePriceMapFuture;
    }
}
