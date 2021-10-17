package com.assignment.api.client;

import com.assignment.api.queue.TrackingAPIQueuEntry;
import com.assignment.api.queue.TrackingAPIQueue;
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
public class TrackingAPIClient extends QueuedAPIClient {

    public static final String TRACKING_QUERY_PARAMETER = "track";

    @Value("${api.queue.delay.tracking}")
    private String TRACKING_QUEUE_DELAY;

    @Value("${api.queue.size.tracking}")
    private String TRACKING_QUEUE_SIZE;

    private Timer sendQueuedAPIRequestsTimer;

    @Autowired
    public TrackingAPIClient(TrackingAPIQueue queue, @Value("${api.url.tracking}") String trackingUrl) {
        super.queue = queue;
        super.queryParameter = TRACKING_QUERY_PARAMETER;
        super.url = trackingUrl;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, String>> getOrderStatusListByOrderId(List<String> orderIdList) {
        TrackingAPIQueue trackingAPIQueue = (TrackingAPIQueue) queue;

        CompletableFuture<Map<String, String>> orderIdTrackingStatusMapFuture;
        if (trackingAPIQueue.getQueue().size() >= Integer.parseInt(TRACKING_QUEUE_SIZE)) {
            orderIdTrackingStatusMapFuture = sendQueuedAPIRequestsWithCurrentRequest(orderIdList);
        } else {
            orderIdTrackingStatusMapFuture = new CompletableFuture<>();

            if (trackingAPIQueue.getQueue().size() == 0) {
                TimerTask task = new TimerTask() {
                    public void run() {
                        sendQueuedAPIRequests();
                    }
                };

                sendQueuedAPIRequestsTimer = new Timer("TrackingAPIQueueTimer");
                sendQueuedAPIRequestsTimer.schedule(task, Long.parseLong(TRACKING_QUEUE_DELAY));

                trackingAPIQueue.getQueue().add(new TrackingAPIQueuEntry(orderIdList, orderIdTrackingStatusMapFuture));
            } else {
                trackingAPIQueue.getQueue().add(new TrackingAPIQueuEntry(orderIdList, orderIdTrackingStatusMapFuture));
            }
        }

        return orderIdTrackingStatusMapFuture;
    }

    private void sendQueuedAPIRequests() {
        TrackingAPIQueue trackingAPIQueue = (TrackingAPIQueue) queue;

        List<TrackingAPIQueuEntry> trackingAPIQueueEntryList = new ArrayList<>();
        Set<String> orderIdSet = new HashSet<>();

        while (trackingAPIQueue.getQueue().peek() != null) {
            TrackingAPIQueuEntry entry = trackingAPIQueue.getQueue().poll();
            trackingAPIQueueEntryList.add(entry);
            orderIdSet.addAll(entry.getApiParameterList());
        }

        resetTimer();

        Map<String, String> orderIdTrackingStatusMap = touchAPI(orderIdSet);

        trackingAPIQueueEntryList.stream().forEach(entry -> {
            entry.getOrderIdTrackingStatusMapFuture().complete(entry.getApiParameterList().stream().collect(Collectors.toMap(parameter -> parameter, orderIdTrackingStatusMap::get)));
        });
    }

    private CompletableFuture<Map<String, String>> sendQueuedAPIRequestsWithCurrentRequest(List<String> orderIdList) {
        TrackingAPIQueue trackingAPIQueue = (TrackingAPIQueue) queue;
        List<TrackingAPIQueuEntry> trackingAPIQueueEntryList = new ArrayList<>();
        Set<String> orderIdSet = new HashSet<>();
        orderIdSet.addAll(orderIdList);

        while (trackingAPIQueue.getQueue().peek() != null) {
            TrackingAPIQueuEntry entry = trackingAPIQueue.getQueue().poll();
            trackingAPIQueueEntryList.add(entry);
            orderIdSet.addAll(entry.getApiParameterList());
        }

        resetTimer();

        Map<String, String> orderIdTrackingStatusMap = touchAPI(orderIdSet);

        trackingAPIQueueEntryList.stream().forEach(entry -> {
            entry.getOrderIdTrackingStatusMapFuture().complete(entry.getApiParameterList().stream().collect(Collectors.toMap(parameter -> parameter, orderIdTrackingStatusMap::get)));
        });

        CompletableFuture<Map<String, String>> orderIdTrackingStatusMapFuture = new CompletableFuture<>();
        orderIdTrackingStatusMapFuture.complete(orderIdList.stream().collect(Collectors.toMap(parameter -> parameter, parameter -> orderIdTrackingStatusMap.get(parameter))));
        return orderIdTrackingStatusMapFuture;
    }

    private Map touchAPI(Set<String> orderIdSet) {
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

    private void resetTimer() {
        if (sendQueuedAPIRequestsTimer != null) {
            sendQueuedAPIRequestsTimer.cancel();
            sendQueuedAPIRequestsTimer.purge();
        }
    }
}
