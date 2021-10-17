package com.assignment.api.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ShipmentAPIQueue implements APIQueue {

    private ConcurrentLinkedQueue<ShipmentAPIQueueEntry> queue;

    public ShipmentAPIQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public ConcurrentLinkedQueue<ShipmentAPIQueueEntry> getQueue() {
        return queue;
    }
}
