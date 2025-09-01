package org.acssz.ezclaim.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.acssz.ezclaim.config.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecretKey jwtSecretKey(JwtProperties props) {
        if (props.getSecret() == null || props.getSecret().isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret must be configured");
        }
        if (!"HS256".equalsIgnoreCase(props.getAlgorithm())) {
            throw new IllegalStateException("Only HS256 is supported for JWT in this setup");
        }
        return new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Anyone can login
                    .requestMatchers("/api/auth/login").permitAll()

                    // Audit endpoints: require AUDIT scope
                    .requestMatchers("/api/audit-events/**").hasAuthority(Scope.AUDIT.authority())

                    // Claims: anonymous can read individual claim; list requires CLAIM_READ; writes require CLAIM_WRITE
                    .requestMatchers(HttpMethod.GET, "/api/claims").hasAuthority(Scope.CLAIM_READ.authority())
                    .requestMatchers(HttpMethod.GET, "/api/claims/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/claims").hasAuthority(Scope.CLAIM_WRITE.authority())
                    .requestMatchers(HttpMethod.PUT, "/api/claims/*").hasAuthority(Scope.CLAIM_WRITE.authority())
                    .requestMatchers(HttpMethod.DELETE, "/api/claims/*").hasAuthority(Scope.CLAIM_WRITE.authority())

                    // Tags (labels): anonymous can read all tags; writes require TAG_WRITE
                    .requestMatchers(HttpMethod.GET, "/api/tags", "/api/tags/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/tags").hasAuthority(Scope.TAG_WRITE.authority())
                    .requestMatchers(HttpMethod.PUT, "/api/tags/*").hasAuthority(Scope.TAG_WRITE.authority())
                    .requestMatchers(HttpMethod.DELETE, "/api/tags/*").hasAuthority(Scope.TAG_WRITE.authority())

                    // Photos: anonymous can read/write individual photo; list requires PHOTO_READ; updates requires PHOTO_WRITE; deletions requires PHOTO_DELETE
                    .requestMatchers(HttpMethod.GET, "/api/photos/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/photos/*/download-url").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/photos/presign-upload").permitAll() // FIXME: require registration
                    .requestMatchers(HttpMethod.GET, "/api/photos").hasAuthority(Scope.PHOTO_READ.authority())
                    .requestMatchers(HttpMethod.DELETE, "/api/photos/*").hasAuthority(Scope.PHOTO_DELETE.authority())
                    .requestMatchers(HttpMethod.POST, "/api/photos").hasAuthority(Scope.PHOTO_WRITE.authority())

                    // other endpoints (if any) default deny unless explicitly allowed
                    .anyRequest().denyAll()
            )
            .httpBasic(b -> b.disable())
            .formLogin(form -> form.disable())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .cors(Customizer.withDefaults());
        return http.build();
    }
}
