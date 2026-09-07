package br.edu.infnet.auth;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class AuthPersistenceIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("authdb")
      .withUsername("auth").withPassword("auth");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  UserRepository users;
  @Autowired
  RefreshTokenRepository refreshTokens;

  @Test
  void persistsUsersAndRefreshTokensInPostgres() {
    var user = users.findByUsername("student");
    assertThat(user).isPresent();
    refreshTokens
        .save(new RefreshToken(null, "test-refresh", "student", java.time.Instant.now().plusSeconds(60), false));
    assertThat(refreshTokens.findByToken("test-refresh")).isPresent();
  }
}
