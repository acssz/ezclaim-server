package org.acssz.ezclaim.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {
  // Hard-coded credentials (demo only)
  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "ezclaim-password";
  private static final String READER_USER = "reader";
  private static final String READER_PASS = "reader-pass";
  private final JwtEncoder jwtEncoder;
  private final org.acssz.ezclaim.config.JwtProperties jwtProps;

  @PostMapping("/login")
  @Operation(
      summary = "Login for demo users",
      description = "Returns a JWT token for demo credentials.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Authenticated"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    String scopes = null;
    String subject = null;
    if (ADMIN_USER.equals(req.getUsername()) && ADMIN_PASS.equals(req.getPassword())) {
      subject = ADMIN_USER;
      scopes =
          String.join(
              " ",
              new String[] {
                // read permissions
                Scope.AUDIT.name(), Scope.TAG_READ.name(), Scope.PHOTO_READ.name(),
                    Scope.CLAIM_READ.name(),
                // elevated permissions
                Scope.CLAIM_WRITE.name(), Scope.TAG_WRITE.name(), Scope.PHOTO_DELETE.name(),
                    Scope.PHOTO_WRITE.name()
              });
    } else if (READER_USER.equals(req.getUsername()) && READER_PASS.equals(req.getPassword())) {
      subject = READER_USER;
      scopes =
          String.join(
              " ",
              new String[] {
                Scope.AUDIT.name(),
                Scope.TAG_READ.name(),
                Scope.PHOTO_READ.name(),
                Scope.CLAIM_READ.name()
              });
    }

    if (scopes != null) {
      Instant now = Instant.now();
      Instant exp = now.plus(jwtProps.getTtl());

      JwtClaimsSet claims =
          JwtClaimsSet.builder()
              .issuer("ezclaim")
              .issuedAt(now)
              .expiresAt(exp)
              .subject(subject)
              // scope is a common claim for authorities; space-delimited string
              .claim("scope", scopes)
              .build();

      JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
      String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

      return ResponseEntity.ok(
          Map.of("token", token, "tokenType", "Bearer", "expiresAt", exp.toString()));
    }
    return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
  }

  @Data
  @Schema(name = "LoginRequest")
  public static class LoginRequest {
    @NotBlank @Schema(example = "admin")
    private String username;

    @NotBlank @Schema(example = "ezclaim-password")
    private String password;
  }
}
