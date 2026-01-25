package dev.gaurav.nityalog.security.jwt;

import com.nimbusds.jwt.SignedJWT;
import dev.gaurav.nityalog.entities.JwtKey;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.JwtKeyStatus;
import dev.gaurav.nityalog.exceptions.*;
import dev.gaurav.nityalog.properties.JwtProperties;
import dev.gaurav.nityalog.repositories.JwtKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final JwtKeyRepository jwtKeyRepository;
    private static final String KEY_ALGORITHM = "RSA";
    private static final SignatureAlgorithm JWT_ALGORITHM = SignatureAlgorithm.RS256;

    public JwtService(
            JwtProperties jwtProperties,
            UserDetailsService userDetailsService,
            JwtKeyRepository jwtKeyRepository
    ) {
        this.jwtProperties = jwtProperties;
        this.jwtKeyRepository = jwtKeyRepository;
    }

    @Transactional(readOnly = true)
    public Optional<JwtKey> loadActiveKey() {
        return jwtKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwtKeyStatus.ACTIVE);
    }

    // Isolation.SERIALIZABLE is used to make sure no other transaction can read or write data until this transaction is complete
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public JwtKey generateAndPersistKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            kpg.initialize(2048, SecureRandom.getInstanceStrong());
            KeyPair keyPair = kpg.generateKeyPair();
            String privatePem = toPkcs8Pem(keyPair.getPrivate());
            String publicPem = toX509Pem(keyPair.getPublic());
            int deactivatedKeysCount = jwtKeyRepository.retireAllActiveKeys();
            log.info("Retired {} old JWT keys", deactivatedKeysCount);
            JwtKey key = JwtKey.builder()
                    .algorithm(JWT_ALGORITHM.getName())
                    .status(JwtKeyStatus.ACTIVE)
                    .publicKeyPem(publicPem)
                    .privateKeyPem(privatePem)
                    .build();
            return jwtKeyRepository.save(key);
        } catch (DataIntegrityViolationException e) {
            // Handle race condition where another node, pod and VM created a key first
            log.warn("Another node rotated key first, reloading active key");
            return jwtKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwtKeyStatus.ACTIVE)
                    .orElseThrow();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    private String toPkcs8Pem(PrivateKey privateKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
    }

    private String toX509Pem(PublicKey publicKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----\n";
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

}
