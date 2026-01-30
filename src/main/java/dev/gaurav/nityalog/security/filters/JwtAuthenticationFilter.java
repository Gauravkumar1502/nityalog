package dev.gaurav.nityalog.security.filters;

import dev.gaurav.nityalog.constants.SecurityConstants;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.exceptions.JwtAuthenticationException;
import dev.gaurav.nityalog.security.jwt.JwtService;
import dev.gaurav.nityalog.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.info("Inside doFilterInternal:: {}",request);
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            Jwt jwt = jwtService.decodeWithRotationSupport(token);
            String username = jwt.getSubject();

            if (username == null) {
                log.warn("JWT token is missing subject");
                throw new JwtAuthenticationException("Invalid token: missing subject");
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.warn("SecurityContext already contains authentication for user: {}", SecurityContextHolder.getContext().getAuthentication().getName());
                throw new JwtAuthenticationException("Authentication already exists");
            }

            User user = userService.loadUserByUsername(username);
            jwtService.validateToken(jwt, user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            log.warn("Token validation error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            throw e;
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            throw new JwtAuthenticationException("Invalid JWT token", ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        log.info("Inside shouldNotFilter:: {}", request);
        // Skip CORS preflight and public endpoints
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        for (String pattern : SecurityConstants.PUBLIC_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
