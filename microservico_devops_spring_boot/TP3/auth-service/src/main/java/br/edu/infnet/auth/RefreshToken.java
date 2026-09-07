package br.edu.infnet.auth;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("refresh_tokens")
public record RefreshToken(@Id Long id, String token, String username, Instant expiresAt, boolean revoked) {
}
