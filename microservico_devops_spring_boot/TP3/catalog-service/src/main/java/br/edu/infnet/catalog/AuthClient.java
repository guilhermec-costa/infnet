package br.edu.infnet.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
class AuthClient {
  private final WebClient client;

  AuthClient(@Value("${auth-service.url}") String url) {
    client = WebClient.builder().baseUrl(url).build();
  }

  Mono<Principal> validate(String header) {
    return client.get().uri("/auth/validate").header(HttpHeaders.AUTHORIZATION, header).retrieve()
        .bodyToMono(Principal.class);
  }

  record Principal(String username, String role) {
  }
}
