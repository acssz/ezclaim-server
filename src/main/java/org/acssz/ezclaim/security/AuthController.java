package org.acssz.ezclaim.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {
    // Hard-coded credentials (demo only)
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "ezclaim-password";
    private static final Duration TTL = Duration.ofHours(12);

    private final JwtEncoder jwtEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        if (USERNAME.equals(req.getUsername()) && PASSWORD.equals(req.getPassword())) {
            Instant now = Instant.now();
            Instant exp = now.plus(TTL);

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("ezclaim")
                    .issuedAt(now)
                    .expiresAt(exp)
                    .subject(USERNAME)
                    // scope is a common claim for authorities; here a single AUDIT role
                    .claim("scope", "AUDIT")
                    .build();

            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
            String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tokenType", "Bearer",
                    "expiresAt", exp.toString()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }
}
