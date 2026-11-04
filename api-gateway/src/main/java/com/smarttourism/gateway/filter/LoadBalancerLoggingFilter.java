package com.smarttourism.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * Runs right after the load balancer has picked an instance.
 * Logs the choice and adds an X-Routed-To response header,
 * so the round-robin distribution is visible to the client.
 */
@Component
public class LoadBalancerLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancerLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI target = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        if (target != null) {
            String instance = target.getHost() + ":" + target.getPort();
            log.info("{} {} -> {}", exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath(), instance);
            exchange.getResponse().getHeaders().set("X-Routed-To", instance);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // ReactiveLoadBalancerClientFilter resolves lb:// to a real host:port; run just after it
        return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1;
    }
}
