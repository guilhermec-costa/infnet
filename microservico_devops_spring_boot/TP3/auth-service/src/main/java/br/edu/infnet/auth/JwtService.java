package br.edu.infnet.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class JwtService {
  private final SecretKey key;
  private final long accessMinutes;

  JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.access-ttl-minutes}") long accessMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessMinutes = accessMinutes;
  }

  String issue(User user) {
    Instant exp = Instant.now().plusSeconds(accessMinutes * 60);
    return Jwts.builder().subject(user.username()).claim("role", user.role()).issuedAt(new Date())
        .expiration(Date.from(exp)).signWith(key).compact();
  }

  Validation validate(String token) {
    try {
      var c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      return new Validation(c.getSubject(), c.get("role", String.class), true);
    } catch (Exception ex) {
      return new Validation(null, null, false);
    }
  }

  record Validation(String username, String role, boolean valid) {
  }
}
