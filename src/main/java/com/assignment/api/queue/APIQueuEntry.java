package com.assignment.api.queue;

import java.util.List;

public class APIQueuEntry {
    List<String> apiParameterList;

    public APIQueuEntry(List<String> apiParameterList) {
        this.apiParameterList = apiParameterList;
    }

    public List<String> getApiParameterList() {
        return apiParameterList;
    }
}
