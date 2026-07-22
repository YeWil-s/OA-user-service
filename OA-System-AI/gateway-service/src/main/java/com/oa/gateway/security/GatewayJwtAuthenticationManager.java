package com.oa.gateway.security;

import com.oa.security.jwt.JwtClaims;
import com.oa.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.Set;

public class GatewayJwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider jwtTokenProvider;

    public GatewayJwtAuthenticationManager(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String token) || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Missing JWT token"));
        }
        try {
            JwtClaims claims = jwtTokenProvider.parseToken(token);
            GatewayAuthenticatedUser principal = new GatewayAuthenticatedUser(
                    claims.getUserId(),
                    claims.getDeptId(),
                    claims.getUsername(),
                    claims.getRoles(),
                    claims.getPermissions()
            );
            Set<GrantedAuthority> authorities = new LinkedHashSet<>();
            if (claims.getRoles() != null) {
                claims.getRoles().stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            if (claims.getPermissions() != null) {
                claims.getPermissions().stream()
                        .filter(permission -> permission != null && !permission.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            return Mono.just(new UsernamePasswordAuthenticationToken(principal, token, authorities));
        } catch (RuntimeException ex) {
            return Mono.error(new BadCredentialsException("Invalid JWT token", ex));
        }
    }
}
