package com.wl2c.elswhereapigatewayserver.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global Filter : {}", config.baseMessage());

            if (config.preLogger()) {
                log.info("Global Filter Start: REQUEST ID -> {}", request.getId());
                log.info("Global Filter Start: REQUEST URI -> {}", request.getURI());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.postLogger()) {
                    log.info("Global Filter End: RESPONSE STATUS CODE -> {}", response.getStatusCode());
                }
            }));
        };
    }

    public record Config(String baseMessage, boolean preLogger, boolean postLogger) {}
}
