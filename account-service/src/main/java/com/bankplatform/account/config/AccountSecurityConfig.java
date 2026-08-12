package com.bankplatform.account.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
public class AccountSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AccountJwtFilter jwtFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Slf4j
    @Component
    public static class AccountJwtFilter extends OncePerRequestFilter {

        private final SecretKey key;

        public AccountJwtFilter(
                @Value("${security.jwt.secret}") String secret
        ) {
            this.key = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest  req,
                HttpServletResponse res,
                FilterChain         chain
        ) throws ServletException, IOException {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    Claims claims = Jwts.parser()
                            .verifyWith(key).build()
                            .parseSignedClaims(header.substring(7))
                            .getPayload();
                    String userId  = claims.getSubject();
                    Object rolesObj = claims.get("roles");
                    List<SimpleGrantedAuthority> authorities =
                            rolesObj instanceof List<?> list
                                    ? list.stream()
                                    .filter(r -> r instanceof String)
                                    .map(r -> new SimpleGrantedAuthority(
                                            "ROLE_" + r))
                                    .toList()
                                    : List.of();
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    userId, null, authorities));
                    MDC.put("userId", userId);
                } catch (JwtException | IllegalArgumentException ex) {
                    log.warn("JWT validation failed: {}", ex.getMessage());
                }
            }
            chain.doFilter(req, res);
        }
    }
}