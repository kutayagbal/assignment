package com.assignment.api.client;

import java.math.BigDecimal;
import java.util.Map;

public class PricingAPIResponse {

    public Map<String, BigDecimal> countryCodePriceMap;

    public PricingAPIResponse(Map<String, BigDecimal> countryCodePriceMap) {
        this.countryCodePriceMap = countryCodePriceMap;
    }

    public Map<String, BigDecimal> getCountryCodePriceMap() {
        return countryCodePriceMap;
    }

    public void setCountryCodePriceMap(Map<String, BigDecimal> countryCodePriceMap) {
        this.countryCodePriceMap = countryCodePriceMap;
    }
}
