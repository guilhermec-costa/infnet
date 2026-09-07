package br.edu.infnet.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
class DemoUserInitializer {
  @Bean
  CommandLineRunner demoUser(UserRepository users) {
    return args -> {
      if (users.findByUsername("student").isEmpty())
        users.save(new User(null, "student", new BCryptPasswordEncoder().encode("spring123"), "USER"));
    };
  }
}
