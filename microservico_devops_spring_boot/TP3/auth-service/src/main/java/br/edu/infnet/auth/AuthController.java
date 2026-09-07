package br.edu.infnet.auth;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
class AuthController {
  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final JwtService jwt;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private final long refreshHours;

  AuthController(UserRepository users, RefreshTokenRepository refreshTokens, JwtService jwt,
      @Value("${jwt.refresh-ttl-hours}") long refreshHours) {
    this.users = users;
    this.refreshTokens = refreshTokens;
    this.jwt = jwt;
    this.refreshHours = refreshHours;
  }

  @PostMapping("/login")
  TokenResponse login(@RequestBody LoginRequest request) {
    User u = users.findByUsername(request.username()).filter(x -> encoder.matches(request.password(), x.passwordHash()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
    return tokensFor(u);
  }

  @PostMapping("/refresh")
  TokenResponse refresh(@RequestBody RefreshRequest request) {
    RefreshToken old = refreshTokens.findByToken(request.refreshToken())
        .filter(x -> !x.revoked() && x.expiresAt().isAfter(Instant.now()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token"));
    refreshTokens.save(new RefreshToken(old.id(), old.token(), old.username(), old.expiresAt(), true));
    return tokensFor(
        users.findByUsername(old.username()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
  }

  @GetMapping("/validate")
  ValidationResponse validate(@RequestHeader("Authorization") String authorization) {
    if (!authorization.startsWith("Bearer "))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    var v = jwt.validate(authorization.substring(7));
    if (!v.valid())
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return new ValidationResponse(v.username(), v.role());
  }

  private TokenResponse tokensFor(User user) {
    String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(SecureRandom.getSeed(48));
    refreshTokens
        .save(new RefreshToken(null, refresh, user.username(), Instant.now().plusSeconds(refreshHours * 3600), false));
    return new TokenResponse(jwt.issue(user), refresh, "Bearer");
  }

  record LoginRequest(String username, String password) {
  }

  record RefreshRequest(String refreshToken) {
  }

  record TokenResponse(String accessToken, String refreshToken, String tokenType) {
  }

  record ValidationResponse(String username, String role) {
  }
}
