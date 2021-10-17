package com.assignment.api.client;

import java.util.Map;

public class TrackingAPIResponse {

    public Map<String, String> orderIdTrackingStatusMap;

    public TrackingAPIResponse() {
    }

    public TrackingAPIResponse(Map<String, String> orderIdTrackingStatusMap) {
        this.orderIdTrackingStatusMap = orderIdTrackingStatusMap;
    }

    public Map<String, String> getOrderIdTrackingStatusMap() {
        return orderIdTrackingStatusMap;
    }

    public void setOrderIdTrackingStatusMap(Map<String, String> orderIdTrackingStatusMap) {
        this.orderIdTrackingStatusMap = orderIdTrackingStatusMap;
    }
}
