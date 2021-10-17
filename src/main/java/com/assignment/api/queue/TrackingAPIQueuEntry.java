package com.assignment.api.queue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TrackingAPIQueuEntry extends APIQueuEntry {

    CompletableFuture<Map<String, String>> orderIdTrackingStatusMapFuture;

    public TrackingAPIQueuEntry(List<String> apiParameterList, CompletableFuture<Map<String, String>> orderIdTrackingStatusMapFuture) {
        super(apiParameterList);
        this.orderIdTrackingStatusMapFuture = orderIdTrackingStatusMapFuture;
    }

    public CompletableFuture<Map<String, String>> getOrderIdTrackingStatusMapFuture() {
        return orderIdTrackingStatusMapFuture;
    }
}
