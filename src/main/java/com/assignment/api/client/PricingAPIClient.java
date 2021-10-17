package com.assignment.api.client;

import com.assignment.api.queue.PricingAPIQueue;
import com.assignment.api.queue.PricingAPIQueueEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
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
public class PricingAPIClient extends QueuedAPIClient {

    public static final String PRICING_QUERY_PARAMETER = "pricing";

    @Value("${api.queue.delay.pricing}")
    private String PRICING_QUEUE_DELAY;

    @Value("${api.queue.size.pricing}")
    private String PRICING_QUEUE_SIZE;

    private Timer sendQueuedAPIRequestsTimer;

    @Autowired
    public PricingAPIClient(PricingAPIQueue queue, @Value("${api.url.pricing}") String pricingUrl) {
        super.queryParameter = PRICING_QUERY_PARAMETER;
        super.queue = queue;
        super.url = pricingUrl;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, BigDecimal>> getPriceListByCountryCode(List<String> countryCodes) {
        PricingAPIQueue pricingAPIQueue = (PricingAPIQueue) queue;

        CompletableFuture<Map<String, BigDecimal>> countryCodePriceMapFuture;
        if (pricingAPIQueue.getQueue().size() >= Integer.parseInt(PRICING_QUEUE_SIZE)) {
            countryCodePriceMapFuture = sendQueuedAPIRequestsWithCurrentRequest(countryCodes);
        } else {
            countryCodePriceMapFuture = new CompletableFuture<>();

            if (pricingAPIQueue.getQueue().size() == 0) {
                TimerTask task = new TimerTask() {
                    public void run() {
                        sendQueuedAPIRequests();
                    }
                };

                sendQueuedAPIRequestsTimer = new Timer("PricingAPIQueueTimer");
                sendQueuedAPIRequestsTimer.schedule(task, Long.parseLong(PRICING_QUEUE_DELAY));

                pricingAPIQueue.getQueue().add(new PricingAPIQueueEntry(countryCodes, countryCodePriceMapFuture));
            } else {
                pricingAPIQueue.getQueue().add(new PricingAPIQueueEntry(countryCodes, countryCodePriceMapFuture));
            }
        }

        return countryCodePriceMapFuture;
    }

    private void sendQueuedAPIRequests() {
        PricingAPIQueue pricingAPIQueue = (PricingAPIQueue) queue;

        List<PricingAPIQueueEntry> pricingAPIQueueEntryList = new ArrayList<>();
        Set<String> countryCodeSet = new HashSet<>();

        while (pricingAPIQueue.getQueue().peek() != null) {
            PricingAPIQueueEntry entry = pricingAPIQueue.getQueue().poll();
            pricingAPIQueueEntryList.add(entry);
            countryCodeSet.addAll(entry.getApiParameterList());
        }

        sendQueuedAPIRequestsTimer.cancel();
        sendQueuedAPIRequestsTimer.purge();

        Map<String, BigDecimal> countryCodePriceMap = touchAPI(countryCodeSet);

        pricingAPIQueueEntryList.stream().forEach(entry -> {
            entry.getCountryCodePriceMapFuture().complete(entry.getApiParameterList().stream().collect(Collectors.toMap(parameter -> parameter, parameter -> countryCodePriceMap.get(parameter))));
        });
    }

    private CompletableFuture<Map<String, BigDecimal>> sendQueuedAPIRequestsWithCurrentRequest(List<String> countryCodes) {
        PricingAPIQueue pricingAPIQueue = (PricingAPIQueue) queue;
        List<PricingAPIQueueEntry> pricingAPIQueueEntryList = new ArrayList<>();
        Set<String> countryCodeSet = new HashSet<>();
        countryCodeSet.addAll(countryCodes);

        while (pricingAPIQueue.getQueue().peek() != null) {
            PricingAPIQueueEntry entry = pricingAPIQueue.getQueue().poll();
            pricingAPIQueueEntryList.add(entry);
            countryCodeSet.addAll(entry.getApiParameterList());
        }

        sendQueuedAPIRequestsTimer.cancel();
        sendQueuedAPIRequestsTimer.purge();

        Map<String, BigDecimal> countryCodePriceMap = touchAPI(countryCodeSet);

        pricingAPIQueueEntryList.stream().forEach(entry -> entry.getCountryCodePriceMapFuture().complete(entry.getApiParameterList().stream().collect(Collectors.toMap(parameter -> parameter, countryCodePriceMap::get))));

        CompletableFuture<Map<String, BigDecimal>> countryCodePriceMapFuture = new CompletableFuture<>();
        countryCodePriceMapFuture.complete(countryCodes.stream().collect(Collectors.toMap(parameter -> parameter, countryCodePriceMap::get)));
        return countryCodePriceMapFuture;
    }

    private Map<String, BigDecimal> touchAPI(Set<String> countryCodeSet) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        builder.queryParam("q", countryCodeSet);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Map.class).getBody();
    }
}
