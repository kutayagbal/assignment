package com.assignment.endpoint;

import com.assignment.service.AggregationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/aggregation")
public class AggregationController {

    private final AggregationService service;

    public AggregationController(AggregationService service) {
        this.service = service;
    }

    @GetMapping
    public AggregationResponse aggregate(@RequestParam(required = false) List<String> pricing, @RequestParam(required = false) List<String> track, @RequestParam(required = false) List<String> shipments)
            throws ExecutionException, InterruptedException {
        return service.aggregate(pricing, track, shipments);
    }
}
