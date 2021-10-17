package com.assignment.endpoint;

import com.assignment.api.client.PricingAPIClient;
import com.assignment.api.client.ShipmentAPIClient;
import com.assignment.api.client.TrackingAPIClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AggregationResponse {

    private Map<String, BigDecimal> countryCodePriceMap;
    private Map<String, String> orderIdTrackingStatusMap;
    private Map<String, List<String>> orderIdShipmentItemListMap;

    public AggregationResponse() {
    }

    public AggregationResponse(Map<String, BigDecimal> countryCodePriceMap, Map<String, String> orderIdTrackingStatusMap, Map<String, List<String>> orderIdShipmentItemListMap) {
        this.countryCodePriceMap = countryCodePriceMap;
        this.orderIdTrackingStatusMap = orderIdTrackingStatusMap;
        this.orderIdShipmentItemListMap = orderIdShipmentItemListMap;
    }

    @JsonProperty(PricingAPIClient.PRICING_QUERY_PARAMETER)
    public Map<String, BigDecimal> getCountryCodePriceMap() {
        return countryCodePriceMap;
    }

    @JsonProperty(TrackingAPIClient.TRACKING_QUERY_PARAMETER)
    public Map<String, String> getOrderIdTrackingStatusMap() {
        return orderIdTrackingStatusMap;
    }

    @JsonProperty(ShipmentAPIClient.SHIPMENTS_QUERY_PARAMETER)
    public Map<String, List<String>> getOrderIdShipmentItemListMap() {
        return orderIdShipmentItemListMap;
    }
}
