package com.assignment.api.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class PricingAPIQueue implements APIQueue {

    private ConcurrentLinkedQueue<PricingAPIQueueEntry> queue;

    public PricingAPIQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public ConcurrentLinkedQueue<PricingAPIQueueEntry> getQueue() {
        return queue;
    }
}
