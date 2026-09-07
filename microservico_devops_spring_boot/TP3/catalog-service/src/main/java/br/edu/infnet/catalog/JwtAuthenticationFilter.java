package br.edu.infnet.catalog;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class JwtAuthenticationFilter implements WebFilter {
  private final AuthClient auth;

  JwtAuthenticationFilter(AuthClient auth) {
    this.auth = auth;
  }

  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (!path.startsWith("/api/"))
      return chain.filter(exchange);
    String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer "))
      return unauthorized(exchange);
    return auth.validate(header).flatMap(p -> {
      exchange.getAttributes().put("principal", p);
      return chain.filter(exchange);
    }).onErrorResume(e -> unauthorized(exchange));
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
