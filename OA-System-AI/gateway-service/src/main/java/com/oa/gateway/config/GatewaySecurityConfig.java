package com.oa.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.gateway.security.GatewayJwtAuthenticationConverter;
import com.oa.gateway.security.GatewayJwtAuthenticationManager;
import com.oa.gateway.security.JsonServerAccessDeniedHandler;
import com.oa.gateway.security.JsonServerAuthenticationEntryPoint;
import com.oa.gateway.security.UserHeaderPropagationSuccessHandler;
import com.oa.security.jwt.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            AuthenticationWebFilter authenticationWebFilter,
            JsonServerAuthenticationEntryPoint authenticationEntryPoint,
            JsonServerAccessDeniedHandler accessDeniedHandler,
            GatewaySecurityProperties securityProperties
    ) {
        return http
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeExchange(authorize -> {
                    for (String pattern : securityProperties.getWhitelist()) {
                        authorize.pathMatchers(pattern).permitAll();
                    }
                    authorize.anyExchange().authenticated();
                })
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter(
            ReactiveAuthenticationManager reactiveAuthenticationManager,
            GatewayJwtAuthenticationConverter authenticationConverter,
            UserHeaderPropagationSuccessHandler successHandler,
            JsonServerAuthenticationEntryPoint authenticationEntryPoint
    ) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(reactiveAuthenticationManager);
        filter.setServerAuthenticationConverter(authenticationConverter);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler((webFilterExchange, exception) ->
                authenticationEntryPoint.commence(webFilterExchange.getExchange(), exception));
        return filter;
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(JwtTokenProvider jwtTokenProvider) {
        return new GatewayJwtAuthenticationManager(jwtTokenProvider);
    }

    @Bean
    public GatewayJwtAuthenticationConverter gatewayJwtAuthenticationConverter(GatewaySecurityProperties securityProperties) {
        return new GatewayJwtAuthenticationConverter(securityProperties.getWhitelist());
    }

    @Bean
    public JsonServerAuthenticationEntryPoint jsonServerAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JsonServerAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public JsonServerAccessDeniedHandler jsonServerAccessDeniedHandler(ObjectMapper objectMapper) {
        return new JsonServerAccessDeniedHandler(objectMapper);
    }

    @Bean
    public UserHeaderPropagationSuccessHandler userHeaderPropagationSuccessHandler() {
        return new UserHeaderPropagationSuccessHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
