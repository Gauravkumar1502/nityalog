package dev.gaurav.nityalog.security.oauth;

import dev.gaurav.nityalog.dtos.ApiResponse;
import dev.gaurav.nityalog.dtos.AuthResponse;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.ProviderType;
import dev.gaurav.nityalog.services.AuthService;
import dev.gaurav.nityalog.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken authToken)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "invalid_authentication_type",
                    "Invalid authentication type: " + authentication.getClass().getSimpleName(),
                    null
            ));
        }
        OAuth2User oauthUser = Optional.ofNullable(authToken.getPrincipal())
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error(
                        "oauth2_principal_missing",
                        "OAuth2 authentication failed: principal is null",
                        null)));
        ProviderType provider = ProviderType.fromOauth2RegistrationId(authToken.getAuthorizedClientRegistrationId());
        log.info("response: {}", objectMapper.writeValueAsString(authToken));
        log.debug("OAuth2 login successful for provider: {}", provider);

        // Process User (Find or Create)
        User user = authService.processOAuth2User(oauthUser, provider);

        // Validate User Status
        userService.validateUserStatus(user);

        AuthResponse authResponse = authService.issueTokens(user);

        // Send Response
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.success(HttpServletResponse.SC_OK, authResponse));
    }
}
