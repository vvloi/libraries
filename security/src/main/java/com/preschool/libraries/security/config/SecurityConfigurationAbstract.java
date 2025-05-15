package com.preschool.libraries.security.config;

import com.preschool.libraries.security.converter.JwtAuthConverter;
import com.preschool.libraries.security.handler.App401AuthenticationEndpointHandler;
import com.preschool.libraries.security.handler.App403AccessDeniedHandler;
import java.util.Arrays;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Component
@Setter(onMethod = @__(@Autowired))
public abstract class SecurityConfigurationAbstract {
  private JwtAuthConverter jwtAuthConverter;
  private App401AuthenticationEndpointHandler authenticationEntryPoint;
  private App403AccessDeniedHandler accessDeniedHandler;

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy(
      SessionRegistry sessionRegistry) {
    return new RegisterSessionAuthenticationStrategy(sessionRegistry);
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public SecurityFilterChain resourceServerFilterChain(
      HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
    return http.cors(
            cors ->
                cors.configurationSource(
                    corsConfigurationSource)) // disable csrf to get permitAll effectively of
        // request matchers public url
        .csrf(csrf -> csrf.ignoringRequestMatchers(publicUrls()))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(publicUrls()).permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(
            (oauth2) ->
                oauth2.jwt(
                    jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)))
        .exceptionHandling(
            handler ->
                handler
                    .accessDeniedHandler(accessDeniedHandler)
                    .authenticationEntryPoint(authenticationEntryPoint))
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  public abstract String[] publicUrls();

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
