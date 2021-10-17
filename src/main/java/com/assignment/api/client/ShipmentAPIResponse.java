package com.assignment.api.client;

import java.util.List;
import java.util.Map;

public class ShipmentAPIResponse {

    public Map<String, List<String>> orderIdShipmentItemListMap;

    public ShipmentAPIResponse() {
    }

    public ShipmentAPIResponse(Map<String, List<String>> orderIdShipmentItemListMap) {
        this.orderIdShipmentItemListMap = orderIdShipmentItemListMap;
    }

    public Map<String, List<String>> getOrderIdShipmentItemListMap() {
        return orderIdShipmentItemListMap;
    }

    public void setOrderIdShipmentItemListMap(Map<String, List<String>> orderIdShipmentItemListMap) {
        this.orderIdShipmentItemListMap = orderIdShipmentItemListMap;
    }
}
