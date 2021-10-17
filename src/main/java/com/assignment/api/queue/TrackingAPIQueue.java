package com.assignment.api.queue;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class TrackingAPIQueue implements APIQueue {

    private ConcurrentLinkedQueue<TrackingAPIQueuEntry> queue;

    public TrackingAPIQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public ConcurrentLinkedQueue<TrackingAPIQueuEntry> getQueue() {
        return queue;
    }
}
