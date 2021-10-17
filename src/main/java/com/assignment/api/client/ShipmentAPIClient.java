package com.assignment.api.client;

import com.assignment.api.queue.ShipmentAPIQueue;
import com.assignment.api.queue.ShipmentAPIQueueEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ShipmentAPIClient extends QueuedAPIClient {

    public static final String SHIPMENTS_QUERY_PARAMETER = "shipments";

    @Value("${api.queue.delay.shipment}")
    private String SHIPMENT_QUEUE_DELAY;

    @Value("${api.queue.size.shipment}")
    private String SHIPMENT_QUEUE_SIZE;

    private Timer sendQueuedAPIRequestsTimer;

    @Autowired
    public ShipmentAPIClient(ShipmentAPIQueue queue, @Value("${api.url.shipments}") String shipmentsUrl) {
        super.queue = queue;
        super.queryParameter = SHIPMENTS_QUERY_PARAMETER;
        super.url = shipmentsUrl;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, List<String>>> getShipmentItemListByOrderId(List<String> orderIdList) {
        ShipmentAPIQueue shipmentAPIQueue = (ShipmentAPIQueue) queue;

        CompletableFuture<Map<String, List<String>>> orderIdTShipmentItemListMapFuture;
        if (shipmentAPIQueue.getQueue().size() >= Integer.parseInt(SHIPMENT_QUEUE_SIZE)) {
            orderIdTShipmentItemListMapFuture = sendQueuedAPIRequestsWithCurrentRequest(orderIdList);
        } else {
            orderIdTShipmentItemListMapFuture = new CompletableFuture<>();

            if (shipmentAPIQueue.getQueue().size() == 0) {
                TimerTask task = new TimerTask() {
                    public void run() {
                        sendQueuedAPIRequests();
                    }
                };

                sendQueuedAPIRequestsTimer = new Timer("ShipmentAPIQueueTimer");
                sendQueuedAPIRequestsTimer.schedule(task, Long.parseLong(SHIPMENT_QUEUE_DELAY));

                shipmentAPIQueue.getQueue().add(new ShipmentAPIQueueEntry(orderIdList, orderIdTShipmentItemListMapFuture));
            } else {
                shipmentAPIQueue.getQueue().add(new ShipmentAPIQueueEntry(orderIdList, orderIdTShipmentItemListMapFuture));
            }
        }

        return orderIdTShipmentItemListMapFuture;
    }

    private void sendQueuedAPIRequests() {
        ShipmentAPIQueue shipmentAPIQueue = (ShipmentAPIQueue) queue;

        List<ShipmentAPIQueueEntry> shipmentAPIQueueEntryList = new ArrayList<>();
        Set<String> orderIdSet = new HashSet<>();

        while (shipmentAPIQueue.getQueue().peek() != null) {
            ShipmentAPIQueueEntry entry = shipmentAPIQueue.getQueue().poll();
            shipmentAPIQueueEntryList.add(entry);
            orderIdSet.addAll(entry.getApiParameterList());
        }

        sendQueuedAPIRequestsTimer.cancel();
        sendQueuedAPIRequestsTimer.purge();

        Map<String, List<String>> orderIdShipmentItemListMap = touchAPI(orderIdSet);

        shipmentAPIQueueEntryList.stream().forEach(entry -> entry.getOrderIdShipmentItemListMapFuture().complete(entry.getApiParameterList().stream().collect(Collectors.toMap(parameter -> parameter, orderIdShipmentItemListMap::get))));
    }

    private CompletableFuture<Map<String, List<String>>> sendQueuedAPIRequestsWithCurrentRequest(List<String> orderIdList) {
        ShipmentAPIQueue shipmentAPIQueue = (ShipmentAPIQueue) queue;
        List<ShipmentAPIQueueEntry> shipmentAPIQueueEntryList = new ArrayList<>();
        Set<String> orderIdSet = new HashSet<>();
        orderIdSet.addAll(orderIdList);

        while (shipmentAPIQueue.getQueue().peek() != null) {
            ShipmentAPIQueueEntry entry = shipmentAPIQueue.getQueue().poll();
            shipmentAPIQueueEntryList.add(entry);
            orderIdSet.addAll(entry.getApiParameterList());
        }

        sendQueuedAPIRequestsTimer.cancel();
        sendQueuedAPIRequestsTimer.purge();

        Map<String, List<String>> orderIdShipmentItemListMap = touchAPI(orderIdSet);

        shipmentAPIQueueEntryList.stream().forEach(entry -> entry.getOrderIdShipmentItemListMapFuture().complete(entry.getApiParameterList().stream().collect(Collectors.toMap(parameter -> parameter, orderIdShipmentItemListMap::get))));

        CompletableFuture<Map<String, List<String>>> orderIdTShipmentItemListMapFuture = new CompletableFuture<>();
        orderIdTShipmentItemListMapFuture.complete(orderIdList.stream().collect(Collectors.toMap(parameter -> parameter, orderIdShipmentItemListMap::get)));
        return orderIdTShipmentItemListMapFuture;
    }

    private Map<String, List<String>> touchAPI(Set<String> orderIdSet) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        builder.queryParam("q", orderIdSet);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Map.class).getBody();
    }
}
