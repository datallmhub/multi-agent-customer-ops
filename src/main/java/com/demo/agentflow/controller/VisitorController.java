package com.demo.agentflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/stats")
public class VisitorController {

    private final AtomicInteger visitorCount = new AtomicInteger(0);

    @GetMapping("/visit")
    public Map<String, Integer> recordVisit() {
        // Increment and return the new count
        return Map.of("visitors", visitorCount.incrementAndGet());
    }
}
