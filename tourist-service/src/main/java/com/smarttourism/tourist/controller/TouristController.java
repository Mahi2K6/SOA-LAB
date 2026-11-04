package com.smarttourism.tourist.controller;

import com.smarttourism.tourist.model.ApiResponse;
import com.smarttourism.tourist.model.TouristPlace;
import com.smarttourism.tourist.service.TouristPlaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tourist")
public class TouristController {

    private final TouristPlaceService service;

    @Value("${eureka.instance.instance-id:tourist-service}")
    private String instanceId;

    private int port;

    public TouristController(TouristPlaceService service) {
        this.service = service;
    }

    @EventListener
    public void onWebServerReady(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }

    private <T> ApiResponse<T> wrap(T data) {
        return new ApiResponse<>(instanceId, port, Instant.now(), data);
    }

    /** Shows which instance answered — call it repeatedly through the gateway. */
    @GetMapping("/instance")
    public ApiResponse<Map<String, Object>> instance() {
        return wrap(Map.of("status", "UP", "message", "Hello from tourist-service on port " + port));
    }

    @GetMapping("/places")
    public ApiResponse<List<TouristPlace>> all(@RequestParam(required = false) String city) {
        return wrap(city == null ? service.findAll() : service.findByCity(city));
    }

    @GetMapping("/places/{id}")
    public ResponseEntity<ApiResponse<TouristPlace>> byId(@PathVariable Long id) {
        return service.findById(id)
                .map(p -> ResponseEntity.ok(wrap(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/places")
    public ResponseEntity<ApiResponse<TouristPlace>> create(@Valid @RequestBody TouristPlace place) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(service.add(place)));
    }

    @PutMapping("/places/{id}")
    public ResponseEntity<ApiResponse<TouristPlace>> update(@PathVariable Long id,
                                                            @Valid @RequestBody TouristPlace place) {
        return service.update(id, place)
                .map(p -> ResponseEntity.ok(wrap(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
