package com.smarttourism.gateway.controller;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Shows what the gateway currently sees in Eureka.
 * GET http://localhost:8080/gateway/services
 * GET http://localhost:8080/gateway/services/tourist-service
 */
@RestController
public class DiscoveryController {

    private final DiscoveryClient discoveryClient;

    public DiscoveryController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/gateway/services")
    public List<String> services() {
        return discoveryClient.getServices();
    }

    @GetMapping("/gateway/services/{name}")
    public Map<String, Object> instances(@PathVariable String name) {
        List<Map<String, Object>> list = discoveryClient.getInstances(name).stream()
                .map((ServiceInstance i) -> Map.<String, Object>of(
                        "instanceId", i.getInstanceId(),
                        "host", i.getHost(),
                        "port", i.getPort(),
                        "uri", i.getUri().toString()))
                .toList();
        return Map.of("service", name, "instanceCount", list.size(), "instances", list);
    }
}
