package dev.gaurav.nityalog.controllers;

import com.nimbusds.jose.jwk.RSAKey;
import dev.gaurav.nityalog.entities.JwtKey;
import dev.gaurav.nityalog.enums.JwtKeyStatus;
import dev.gaurav.nityalog.repositories.JwtKeyRepository;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JwksController {

    private final JwtKeyRepository jwtKeyRepository;

    public JwksController(JwtKeyRepository jwtKeyRepository) {
        this.jwtKeyRepository = jwtKeyRepository;
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        try {
            List<JwtKey> keys = jwtKeyRepository.findByStatusInOrderByCreatedAtDesc(List.of(JwtKeyStatus.ACTIVE, JwtKeyStatus.RETIRED));
            List<Map<String, Object>> keyJson = keys.stream()
                    .map(JwtKey::toRsaJwk)
                    .map(RSAKey::toPublicJWK)
                    .map(RSAKey::toJSONObject)
                    .toList();
            return Map.of("keys", keyJson);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build JWKS", e);
        }
    }
}
