package com.assignment.api.client;

import com.assignment.api.queue.APIQueue;

public abstract class QueuedAPIClient extends APIClient {
    APIQueue queue;
}
