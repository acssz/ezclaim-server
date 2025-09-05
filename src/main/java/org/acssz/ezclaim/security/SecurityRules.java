package org.acssz.ezclaim.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

public final class SecurityRules {
  private SecurityRules() {}

  public static void apply(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          auth) {
    auth
        // Allow CORS preflight requests
        .requestMatchers(HttpMethod.OPTIONS, "/**")
        .permitAll()

        // OpenAPI & Swagger UI
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
        .permitAll()
        .requestMatchers("/api/auth/login")
        .permitAll()

        // Audit
        .requestMatchers("/api/audit-events/**")
        .hasAuthority(Scope.AUDIT.authority())

        // Claims: list requires CLAIM_READ; single get is public; writes require CLAIM_WRITE
        .requestMatchers(HttpMethod.GET, "/api/claims")
        .hasAuthority(Scope.CLAIM_READ.authority())
        .requestMatchers(HttpMethod.GET, "/api/claims/*")
        .permitAll()
        // Anonymous can create claims
        .requestMatchers(HttpMethod.POST, "/api/claims")
        .permitAll()
        // PATCH is permitted for anonymous, but controller enforces password/role checks for
        // allowed fields and transitions
        .requestMatchers(HttpMethod.PATCH, "/api/claims/*")
        .permitAll()
        .requestMatchers(HttpMethod.PUT, "/api/claims/*")
        .hasAuthority(Scope.CLAIM_WRITE.authority())
        .requestMatchers(HttpMethod.DELETE, "/api/claims/*")
        .hasAuthority(Scope.CLAIM_WRITE.authority())

        // Tags (labels): anonymous can read; writes require TAG_WRITE
        .requestMatchers(HttpMethod.GET, "/api/tags", "/api/tags/*")
        .permitAll()
        .requestMatchers(HttpMethod.POST, "/api/tags")
        .hasAuthority(Scope.TAG_WRITE.authority())
        .requestMatchers(HttpMethod.PUT, "/api/tags/*")
        .hasAuthority(Scope.TAG_WRITE.authority())
        .requestMatchers(HttpMethod.DELETE, "/api/tags/*")
        .hasAuthority(Scope.TAG_WRITE.authority())

        // Photos: anonymous can access individual read and presign-upload; list protected;
        // delete/write scoped
        .requestMatchers(HttpMethod.GET, "/api/photos/*")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/api/photos/*/download-url")
        .permitAll()
        .requestMatchers(HttpMethod.POST, "/api/photos/presign-upload")
        .permitAll()
        .requestMatchers(HttpMethod.POST, "/api/photos")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/api/photos")
        .hasAuthority(Scope.PHOTO_READ.authority())
        .requestMatchers(HttpMethod.DELETE, "/api/photos/*")
        .hasAuthority(Scope.PHOTO_DELETE.authority())

        // Default deny
        .anyRequest()
        .denyAll();
  }
}
