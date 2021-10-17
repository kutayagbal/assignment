package com.assignment.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

abstract class APIClient {

    public String url;
    public String queryParameter;

    @Autowired RestTemplate restTemplate;
}
