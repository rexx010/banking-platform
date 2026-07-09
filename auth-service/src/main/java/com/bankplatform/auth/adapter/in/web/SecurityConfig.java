package com.bankplatform.auth.adapter.in.web;

import com.bankplatform.auth.application.usecase.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
//    these endpoints do not require a token
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter
    ) throws Exception{
        return http
                /*
                 Disable CSRF — Cross-Site Request Forgery protection.
                 CSRF attacks exploit browser session cookies. Since we
                 use JWTs in the Authorization header (not cookies),
                 CSRF is not applicable to our API. Disabling it removes
                 unnecessary overhead.
                 */
                .csrf(AbstractHttpConfigurer::disable)
                /*
                STATELESS sessions — Spring Security must never create
                an HTTP session or use cookies for authentication.
                Every request proves its identity via the JWT token alone.
                This is what makes the API horizontally scalable —
                any server instance can handle any request because
                there is no session state to share between instances.
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no token needed
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // CORS preflight requests — browsers send OPTIONS
                        // before cross-origin requests to check permissions
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                /*
                 Register our JWT filter BEFORE Spring's built-in
                 username/password filter. This ensures the JWT is
                 validated and the user identity is established before
                 any other security processing happens.
                 */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    /**
     BCrypt password encoder with strength 12.
     Strength 12 means 2^12 = 4096 hashing rounds.
     Takes ~250ms per hash — fast enough for login,
     slow enough to make brute force impractical.

     Default strength is 10. We use 12 for better security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

//    JWT AUTHENTICATION FILTER
    /**
     Reads the JWT from the Authorization header on every request,
     validates it, and tells Spring Security who the caller is...
     OncePerRequestFilter guarantees this runs exactly once
     per request even in forward/include chains.
     */
    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class JwtAuthenticationFilter extends OncePerRequestFilter{
        private final JwtService jwtService;

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            /*
             If no Authorization header or it does not start with
             "Bearer ", skip JWT processing entirely.
             Spring Security will handle the request as anonymous —
             public endpoints will still work, protected ones will
             return 401 Unauthorized.
             */
            if (authHeader == null || !authHeader.startsWith("Bearer ")){
                filterChain.doFilter(request, response);
                return;
            }
//            Strip "bearer " prefix to get the raw token
            String token = authHeader.substring(7);
            /*
             Validate the token. validateToken() returns empty
             if the signature is wrong or the token is expired.
             We use ifPresent so no exception propagates here —
             invalid tokens simply result in an anonymous request.
             */
            jwtService.validateToken(token).ifPresent(claims -> {
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities =
                        roles == null ? List.of() :
                                roles.stream()
                                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                        .toList();
                /*
                Create an Authentication object and place it in
                Spring Security's context for this thread.
                After this line, Spring Security considers the
                request authenticated.

                The principal (first argument) is the userId string.
                This is what @AuthenticationPrincipal injects into
                controller methods.
                 */
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                );
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
                // Add userId to MDC so every log line in this
                // request automatically includes the user identity
                MDC.put("userId", userId);

                log.debug("JWT authenticated userId={} email={}",
                        userId, email);
            });
            filterChain.doFilter(request, response);
        }
    }
}
