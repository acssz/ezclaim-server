package org.acssz.ezclaim.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import org.acssz.ezclaim.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired MockMvc mvc;

  @Autowired JwtEncoder jwtEncoder;
  @Autowired JwtProperties jwtProps;

  @TestConfiguration
  static class JwtTestConfig {
    @Bean
    JwtEncoder jwtEncoder() {
      return org.mockito.Mockito.mock(JwtEncoder.class);
    }

    @Bean
    JwtProperties jwtProperties() {
      JwtProperties p = new JwtProperties();
      p.setSecret("test-secret-32-bytes-minimum-1234567890");
      p.setAlgorithm("HS256");
      p.setTtl(java.time.Duration.ofHours(1));
      return p;
    }
  }

  @Test
  void login_success_returns_bearer_token() throws Exception {
    Instant now = Instant.now();
    Jwt jwt =
        new Jwt(
            "test-token",
            now,
            now.plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", "admin", "scope", "AUDIT"));
    when(jwtEncoder.encode(any())).thenReturn(jwt);

    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"ezclaim-password\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("test-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresAt").exists());
  }

  @Test
  void login_invalid_credentials_returns_401() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("invalid_credentials"));
  }
}
