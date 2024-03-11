package com.preschool.library.webutils;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

@Slf4j
public class AppGrantedAuthorityMapper implements GrantedAuthoritiesMapper {
    private static final String GROUPS = "groups";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        GrantedAuthority authority = authorities.iterator().next();
        return Optional.of(authority)
                .filter(OidcUserAuthority.class::isInstance)
                .flatMap(this::parseOidcUserAuthority)
                .orElse(parseOAuth2UserAuthority(authority));
    }

    private Set<GrantedAuthority> parseToAuthority(Collection<String> roles) {
        log.info("process for user has roles {}", roles);
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
        return mappedAuthorities;
    }

    private Collection<GrantedAuthority> generateAuthoritiesFromClaim(Collection<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> parseOAuth2UserAuthority(GrantedAuthority authority) {
        return Optional.ofNullable(authority)
                .map(OAuth2UserAuthority.class::cast)
                .map(OAuth2UserAuthority::getAttributes)
                .filter(userAttributes -> userAttributes.containsKey(REALM_ACCESS_CLAIM))
                .map(userAttributes -> (Map<String, Object>) userAttributes.get(REALM_ACCESS_CLAIM))
                .map(realmAccess -> (Collection<String>) realmAccess.get(ROLES_CLAIM))
                .map(this::parseToAuthority)
                .orElse(Collections.emptySet());
    }

    private Optional<Set<GrantedAuthority>> parseOidcUserAuthority(GrantedAuthority authority) {
        return Optional.ofNullable(authority)
                .map(OidcUserAuthority.class::cast)
                .map(OidcUserAuthority::getUserInfo)
                .filter(userInfo -> userInfo.hasClaim(REALM_ACCESS_CLAIM) || userInfo.hasClaim(GROUPS))
                .flatMap(this::combineUserAndGroupsPermission);
    }

    private Optional<Set<GrantedAuthority>> combineUserAndGroupsPermission(OidcUserInfo userInfo) {
        return extractRolesFromJwt(userInfo).map(this::parseToAuthority);
    }

    @SuppressWarnings("unchecked")
    private Optional<Collection<String>> extractRolesFromJwt(OidcUserInfo userInfo) {
        // Tokens can be configured to return roles under
        // Groups or REALM ACCESS hence have to check both
        return Optional.ofNullable(
                Optional.of(userInfo.hasClaim(REALM_ACCESS_CLAIM))
                        .filter(Boolean::booleanValue)
                        .map(userRolesClaim -> userInfo.getClaimAsMap(REALM_ACCESS_CLAIM))
                        .map(userRoles -> (Collection<String>) userRoles.get(ROLES_CLAIM))
                        .orElse(userInfo.getClaim(GROUPS)));
    }
}
