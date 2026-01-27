package dev.gaurav.nityalog.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import dev.gaurav.nityalog.entities.JwtKey;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class JwtKeyManager {
    private final RsaKeyGenerator rsaKeyGenerator;
    private volatile JwtKey activeKey;
    private volatile JwtEncoder encoder;

    public JwtKeyManager(RsaKeyGenerator rsaKeyGenerator) {
        this.rsaKeyGenerator = rsaKeyGenerator;
    }

    @PostConstruct
    public void init() {
       loadOrCreateActiveKey();
    }

    /**
     * Loads the active JWT key from the database or creates a new one if none exists.
     * Synchronized to ensure thread safety during initialization.
     * using synchronized make sure only one thread can access this method at a time
     */
    private synchronized void loadOrCreateActiveKey() {
        log.info("Loading or creating active JWT key...");
        this.activeKey = rsaKeyGenerator.loadActiveKey()
                .orElseGet(rsaKeyGenerator::generateAndPersistKey);

        log.info("Active JWT key loaded: {}", activeKey.getId());
        refreshEncoder();
    }

    private void refreshEncoder() {
        RSAKey rsaJwk = this.activeKey.toRsaJwk();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaJwk)));
        log.info("JWT Encoder refreshed with key ID: {}", activeKey.getId());
    }

    @Scheduled(cron = "${app.jwt.key-rotation-cron}")
    public synchronized void rotateKey() {
        try {
            log.info("Rotating JWT key...");
            this.activeKey = rsaKeyGenerator.generateAndPersistKey();
            log.info("New JWT key created: {}", this.activeKey.getId());
            refreshEncoder();
        } catch (Exception e) {
            log.error("JWT key rotation failed, continuing with previous key", e);
        }
    }

    public JwtEncoder getEncoder() {
        JwtEncoder local = encoder;
        if (local == null) {
            throw new IllegalStateException("JWT Encoder not initialized");
        }
        return local;
    }

    public JwtKey getActiveKey() {
        JwtKey local = activeKey;
        if (local == null) {
            throw new IllegalStateException("JWT Key not initialized");
        }
        return local;
    }

    public UUID getActiveKeyId() {
        return activeKey.getId();
    }
}
