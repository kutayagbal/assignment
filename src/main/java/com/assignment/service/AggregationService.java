package com.assignment.service;

import com.assignment.endpoint.AggregationResponse;
import com.assignment.api.client.PricingAPIClient;
import com.assignment.api.client.ShipmentAPIClient;
import com.assignment.api.client.TrackingAPIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AggregationService {

    private final PricingAPIClient pricingAPIClient;
    private final TrackingAPIClient trackingAPIClient;
    private final ShipmentAPIClient shipmentsAPIClient;

    @Autowired
    public AggregationService(PricingAPIClient pricingAPIClient, TrackingAPIClient trackingAPIClient, ShipmentAPIClient shipmentsAPIClient) {
        this.pricingAPIClient = pricingAPIClient;
        this.trackingAPIClient = trackingAPIClient;
        this.shipmentsAPIClient = shipmentsAPIClient;
    }

    public AggregationResponse aggregate(List<String> pricingParameters, List<String> trackingParameters, List<String> shipmentsParameters) throws ExecutionException, InterruptedException {
        CompletableFuture<Map<String, BigDecimal>> countryCodePriceMapFuture = null;
        CompletableFuture<Map<String, String>> orderIdTrackingStatusMapFuture = null;
        CompletableFuture<Map<String, List<String>>> orderIdShipmentItemListMapFuture = null;
        List<CompletableFuture> futuresToBeWaited = new ArrayList<>();

        if (pricingParameters != null && !pricingParameters.isEmpty()) {
            countryCodePriceMapFuture = pricingAPIClient.getPriceListByCountryCode(pricingParameters);
            futuresToBeWaited.add(countryCodePriceMapFuture);
        }

        if (trackingParameters != null && !trackingParameters.isEmpty()) {
            orderIdTrackingStatusMapFuture = trackingAPIClient.getOrderStatusListByOrderId(trackingParameters);
            futuresToBeWaited.add(orderIdTrackingStatusMapFuture);
        }

        if (shipmentsParameters != null && !shipmentsParameters.isEmpty()) {
            orderIdShipmentItemListMapFuture = shipmentsAPIClient.getShipmentItemListByOrderId(shipmentsParameters);
            futuresToBeWaited.add(orderIdShipmentItemListMapFuture);
        }

        CompletableFuture.allOf(futuresToBeWaited.toArray(new CompletableFuture[futuresToBeWaited.size()])).join();

        return new AggregationResponse(countryCodePriceMapFuture != null ? countryCodePriceMapFuture.get() : null,
                                       orderIdTrackingStatusMapFuture != null ? orderIdTrackingStatusMapFuture.get() : null,
                                       orderIdShipmentItemListMapFuture != null ? orderIdShipmentItemListMapFuture.get() : null);
    }
}
