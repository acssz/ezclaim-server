package org.acssz.ezclaim.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(props.getAllowedOrigins());
    cfg.setAllowedMethods(props.getAllowedMethods());
    cfg.setAllowedHeaders(props.getAllowedHeaders());
    cfg.setExposedHeaders(props.getExposedHeaders());
    cfg.setAllowCredentials(props.isAllowCredentials());
    cfg.setMaxAge(props.getMaxAge().toSeconds());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
