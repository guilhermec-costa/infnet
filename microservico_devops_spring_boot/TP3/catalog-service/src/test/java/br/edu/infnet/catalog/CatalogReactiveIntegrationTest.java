package br.edu.infnet.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CatalogReactiveIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("catalogdb")
      .withUsername("catalog").withPassword("catalog");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry r) {
    r.add("spring.r2dbc.url",
        () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/catalogdb");
    r.add("spring.r2dbc.username", postgres::getUsername);
    r.add("spring.r2dbc.password", postgres::getPassword);
  }

  @Autowired
  ProductRepository products;
  @LocalServerPort
  int port;

  @Test
  void persistsUsingR2dbc() {
    StepVerifier
        .create(products.save(new Product(null, "Produto de teste", java.math.BigDecimal.TEN))
            .flatMap(p -> products.findById(p.id())))
        .assertNext(p -> assertThat(p.name()).isEqualTo("Produto de teste")).verifyComplete();
  }

  @Test
  void rejectsProtectedRouteWithoutBearerToken() {
    WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build().get().uri("/api/products").exchange()
        .expectStatus().isUnauthorized();
  }
}
