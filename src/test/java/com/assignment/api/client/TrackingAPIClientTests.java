package com.assignment.api.client;

import com.assignment.api.client.TrackingAPIClient;
import com.assignment.api.queue.TrackingAPIQueuEntry;
import com.assignment.api.queue.TrackingAPIQueue;
import org.junit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrackingAPIClientTests {

    @Value("${api.url.tracking}")
    private String trackingUrl;

    @Value("${api.queue.delay.tracking}")
    private String TRACKING_QUEUE_DELAY;

    @Autowired
    private TrackingAPIClient client;

    @MockBean
    private RestTemplate mockRestTemplate;

    @SpyBean
    private TrackingAPIQueue mockTrackingAPIQueue;

    @Test
    public void whenQueueIsFull() throws Exception {
        String mockOrderId1 = "12345";
        String mockOrderId2 = "54321";
        String mockTrackingStatus1 = "MOCK_STAT_1";
        String mockTrackingStatus2 = "MOCK_STAT_2";

        Map<String, String> mockOrderIdTrackingStatusMap = new HashMap<>();
        mockOrderIdTrackingStatusMap.put(mockOrderId1, mockTrackingStatus1);
        mockOrderIdTrackingStatusMap.put(mockOrderId2, mockTrackingStatus2);

        ConcurrentLinkedQueue<TrackingAPIQueuEntry> mockQueue = new ConcurrentLinkedQueue<>();
        CompletableFuture<Map<String, String>> mockOrderIdTrackingStatusMapFuture1 = new CompletableFuture<>();
        CompletableFuture<Map<String, String>> mockOrderIdTrackingStatusMapFuture2 = new CompletableFuture<>();
        mockQueue.add(new TrackingAPIQueuEntry(Collections.singletonList(mockOrderId1), mockOrderIdTrackingStatusMapFuture1));
        mockQueue.add(new TrackingAPIQueuEntry(Collections.singletonList(mockOrderId2), mockOrderIdTrackingStatusMapFuture2));
        Mockito.when(mockTrackingAPIQueue.getQueue()).thenReturn(mockQueue);

        Mockito.when(mockRestTemplate.exchange(startsWith(trackingUrl),
                                               eq(HttpMethod.GET),
                                               any(),
                                               eq(Map.class)))
               .thenReturn(new ResponseEntity(mockOrderIdTrackingStatusMap, HttpStatus.OK));

        Map<String, String> actualOrderIdTrackingStatusMap = client.getOrderStatusListByOrderId(Arrays.asList(mockOrderId1, mockOrderId2)).get();
        Assertions.assertEquals(mockOrderIdTrackingStatusMap, actualOrderIdTrackingStatusMap);
    }

    @Test
    public void whenQueueIsEmpty() throws Exception {
        String mockOrderId1 = "12345";
        String mockOrderId2 = "54321";
        String mockTrackingStatus1 = "MOCK_STAT_1";
        String mockTrackingStatus2 = "MOCK_STAT_2";

        Map<String, String> mockOrderIdTrackingStatusMap = new HashMap<>();
        mockOrderIdTrackingStatusMap.put(mockOrderId1, mockTrackingStatus1);
        mockOrderIdTrackingStatusMap.put(mockOrderId2, mockTrackingStatus2);

        Mockito.when(mockTrackingAPIQueue.getQueue()).thenReturn(new ConcurrentLinkedQueue<>());

        Mockito.when(mockRestTemplate.exchange(startsWith(trackingUrl),
                                               eq(HttpMethod.GET),
                                               any(),
                                               eq(Map.class)))
               .thenReturn(new ResponseEntity(mockOrderIdTrackingStatusMap, HttpStatus.OK));

        Map<String, String> actualOrderIdTrackingStatusMap = client.getOrderStatusListByOrderId(Arrays.asList(mockOrderId1, mockOrderId2)).get();
        Assertions.assertEquals(mockOrderIdTrackingStatusMap, actualOrderIdTrackingStatusMap);

        verify(mockRestTemplate, timeout(Integer.parseInt(TRACKING_QUEUE_DELAY))).exchange(startsWith(trackingUrl), eq(HttpMethod.GET), any(), eq(Map.class));
    }
}
