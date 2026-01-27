package dev.gaurav.nityalog.security.jwt;

import dev.gaurav.nityalog.entities.JwtKey;
import dev.gaurav.nityalog.enums.JwtKeyStatus;
import dev.gaurav.nityalog.repositories.JwtKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
public class RsaKeyGenerator {
    private final JwtKeyRepository jwtKeyRepository;
    private static final String KEY_ALGORITHM = "RSA";
    private static final SignatureAlgorithm JWT_ALGORITHM = SignatureAlgorithm.RS256;

    public RsaKeyGenerator(JwtKeyRepository jwtKeyRepository) {
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
            log.warn("Another node rotated key first, reloading active key");
            return jwtKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwtKeyStatus.ACTIVE)
                    .orElseThrow();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    private String toPkcs8Pem(PrivateKey privateKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
    }

    private String toX509Pem(PublicKey publicKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----\n";
    }
}
