package com.preschool.libraries.security.config;

import com.preschool.libraries.security.converter.JwtAuthConverter;
import com.preschool.libraries.security.handler.App401AuthenticationEndpointHandler;
import com.preschool.libraries.security.handler.App403AccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@RequiredArgsConstructor
public abstract class SecurityConfigurationAbstract {
    private final JwtAuthConverter jwtAuthConverter;
    private final App401AuthenticationEndpointHandler authenticationEntryPoint;
    private final App403AccessDeniedHandler accessDeniedHandler;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                // disable csrf to get permitAll effectively of request matchers public url
                .csrf(csrf -> csrf.ignoringRequestMatchers(publicUrls()))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(publicUrls())
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
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
}
