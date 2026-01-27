package dev.gaurav.nityalog.security.jwt;

import com.nimbusds.jwt.SignedJWT;
import dev.gaurav.nityalog.entities.JwtKey;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.JwtKeyStatus;
import dev.gaurav.nityalog.enums.TokenType;
import dev.gaurav.nityalog.exceptions.*;
import dev.gaurav.nityalog.models.TokenData;
import dev.gaurav.nityalog.properties.JwtProperties;
import dev.gaurav.nityalog.repositories.JwtKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final JwtKeyRepository jwtKeyRepository;
    private static final SignatureAlgorithm JWT_ALGORITHM = SignatureAlgorithm.RS256;
    private final JwtKeyManager jwtKeyManager;

    public JwtService(
            JwtProperties jwtProperties,
            UserDetailsService userDetailsService,
            JwtKeyRepository jwtKeyRepository,
            JwtKeyManager jwtKeyManager) {
        this.jwtProperties = jwtProperties;
        this.jwtKeyRepository = jwtKeyRepository;
        this.jwtKeyManager = jwtKeyManager;
    }

    public Jwt decodeWithRotationSupport(String token) {
        try {
            SignedJWT parsed = SignedJWT.parse(token);
            String kid = parsed.getHeader().getKeyID();
            if (kid == null || kid.isBlank()) {
                throw new BadJwtException("Missing key ID (kid) in token header");
            }
            JwtKey jwtKey = jwtKeyRepository
                    .findByIdAndStatusIn(
                            UUID.fromString(kid),
                            List.of(JwtKeyStatus.ACTIVE, JwtKeyStatus.RETIRED))
                    .orElseThrow(() -> new BadJwtException("Key is invalid or revoked"));
            JwtDecoder decoder = getJwtDecoder(jwtKey);
            return decoder.decode(token);
        } catch (BadJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new BadJwtException("Token verification failed", e);
        }
    }

    private JwtDecoder getJwtDecoder(JwtKey jwtKey) {
        return NimbusJwtDecoder
                .withPublicKey((RSAPublicKey) jwtKey.toPublicKey())
                .signatureAlgorithm(JWT_ALGORITHM)
                .build();
    }

    public boolean isTokenValid(Jwt jwt, User user) {
        return jwt.getExpiresAt() != null &&
           jwt.getExpiresAt().isAfter(Instant.now()) &&
           jwt.getIssuer() != null &&
           jwt.getIssuer().equals(jwtProperties.issuer()) &&
           jwt.getAudience() != null &&
           jwt.getAudience().contains(jwtProperties.audience()) &&
           user.getUsername().equals(jwt.getSubject());
    }

    public void validateToken(Jwt jwt, User user) {
        Instant now = Instant.now();
        if (jwt.getExpiresAt() == null || jwt.getExpiresAt().isBefore(now)) {
            throw new TokenExpiredException("JWT token has expired");
        }

        if (jwt.getNotBefore() != null && jwt.getNotBefore().isAfter(now)) {
            throw new TokenNotActiveException("JWT token is not active yet");
        }

        if (jwt.getIssuedAt() != null &&
                jwt.getIssuedAt().isAfter(now.plusSeconds(30))) {
            throw new TokenIssuedInFutureException("JWT token was issued in the future");
        }

        if (jwt.getIssuer() == null || !jwt.getIssuer().equals(jwtProperties.issuer())) {
            throw new InvalidIssuerException("Invalid JWT issuer");
        }

        if (jwt.getAudience() == null || !jwt.getAudience().contains(jwtProperties.audience())) {
            throw new InvalidAudienceException("Invalid JWT audience");
        }

        if (jwt.getSubject() == null || !jwt.getSubject().equals(user.getUsername())) {
            throw new InvalidSubjectException("JWT subject does not match authenticated user");
        }
    }

    public TokenData generateAccessToken(User user) {
        return generateAccessToken(user, Map.of());
    }

    public TokenData generateAccessToken(User user, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant exp = now.plus(jwtProperties.accessTtl());
        UUID jti = UUID.randomUUID();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer().toString())
                .audience(List.of(jwtProperties.audience()))
                .issuedAt(now)
                .expiresAt(exp)
                .id(jti.toString())
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("type", "access")
                .claim("role", user.getRole().name());

        additionalClaims.forEach(claims::claim);

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .type("JWT")
                .keyId(jwtKeyManager.getActiveKeyId().toString())
                .build();
        return new TokenData(TokenType.ACCESS,
                jwtKeyManager.getEncoder().encode(JwtEncoderParameters.from(headers, claims.build())).getTokenValue(),
                jti,
                jwtProperties.accessTtl()
        );
    }

    public TokenData generateRefreshToken(User user) {
        return generateRefreshToken(user, Map.of());
    }

    public TokenData generateRefreshToken(User user, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant exp = now.plus(jwtProperties.refreshTtl());
        UUID jti = UUID.randomUUID();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer().toString())
                .audience(List.of(jwtProperties.audience()))
                .issuedAt(now)
                .expiresAt(exp)
                .id(jti.toString())
                .subject(user.getId().toString())
                .claim("type", "refresh");

        additionalClaims.forEach(claims::claim);

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .type("JWT")
                .keyId(jwtKeyManager.getActiveKeyId().toString())
                .build();

        return new TokenData(TokenType.REFRESH,
                jwtKeyManager.getEncoder().encode(JwtEncoderParameters.from(headers, claims.build())).getTokenValue(),
                jti,
                jwtProperties.refreshTtl()
        );
    }

}
