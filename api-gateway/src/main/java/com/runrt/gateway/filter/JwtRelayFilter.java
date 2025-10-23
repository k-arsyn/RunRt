package com.runrt.gateway.filter;

import com.runrt.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtRelayFilter extends AbstractGatewayFilterFactory<JwtRelayFilter.Config> {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtRelayFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config { }

    @Override
    public GatewayFilter apply(Config config) {
        return this::filter;
    }

    private Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseClaims(token);
                String uid = (String) claims.get("uid");
                String role = (String) claims.get("role");
                ServerHttpRequest mutated = exchange.getRequest().mutate()
                        .header("X-User-Id", uid != null ? uid : "")
                        .header("X-User-Role", role != null ? role : "")
                        .build();
                return chain.filter(exchange.mutate().request(mutated).build());
            } catch (Exception ignored) {
                // fall through without headers if token invalid
            }
        }
        return chain.filter(exchange);
    }
}
