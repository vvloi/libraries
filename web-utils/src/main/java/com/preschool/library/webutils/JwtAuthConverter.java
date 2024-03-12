package com.preschool.library.webutils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String GROUPS = "groups";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    public JwtAuthConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, getAuthorityFromJwt(jwt));
    }

    private Set<GrantedAuthority> getAuthorityFromJwt(Jwt jwt) {
        return Stream.of(extractRealmAccessRoles(jwt), extractGroupRoles(jwt))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<GrantedAuthority> extractRealmAccessRoles(Jwt jwt) {
        return extractRoleClaimOfRealmAccess(jwt)
                .map(this::parseToAuthority)
                .orElse(Collections.emptySet());
    }

    @SuppressWarnings("unchecked")
    private Optional<Collection<String>> extractRoleClaimOfRealmAccess(Jwt jwt) {
        return Optional.of(jwt.hasClaim(REALM_ACCESS_CLAIM))
                .filter(Boolean::booleanValue)
                .map(userRolesClaim -> jwt.getClaimAsMap(REALM_ACCESS_CLAIM))
                .map(userRoles -> (Collection<String>) userRoles.get(ROLES_CLAIM));
    }

    private Set<GrantedAuthority> extractGroupRoles(Jwt jwt) {
        return extractRoleClaimOfGroup(jwt).map(this::parseToAuthority).orElse(Collections.emptySet());
    }

    private Optional<Collection<String>> extractRoleClaimOfGroup(Jwt jwt) {
        return Optional.of(jwt.hasClaim(GROUPS))
                .filter(Boolean::booleanValue)
                .map(userRolesClaim -> jwt.getClaim(GROUPS));
    }

    private Set<GrantedAuthority> parseToAuthority(Collection<String> roles) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
        return mappedAuthorities;
    }

    private Collection<GrantedAuthority> generateAuthoritiesFromClaim(Collection<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
