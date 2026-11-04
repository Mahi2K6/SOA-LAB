package com.smarttourism.tourist.model;

import java.time.Instant;

/**
 * Wraps every response with the instance that served it,
 * so you can see the gateway rotating between instances.
 */
public record ApiResponse<T>(String servedBy, int port, Instant timestamp, T data) {
}
