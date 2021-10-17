package com.assignment.api.queue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShipmentAPIQueueEntry extends APIQueuEntry {

    CompletableFuture<Map<String, List<String>>> orderIdShipmentItemListMapFuture;

    public ShipmentAPIQueueEntry(List<String> apiParameterList, CompletableFuture<Map<String, List<String>>> orderIdShipmentItemListMapFuture) {
        super(apiParameterList);
        this.orderIdShipmentItemListMapFuture = orderIdShipmentItemListMapFuture;
    }

    public CompletableFuture<Map<String, List<String>>> getOrderIdShipmentItemListMapFuture() {
        return orderIdShipmentItemListMapFuture;
    }
}
