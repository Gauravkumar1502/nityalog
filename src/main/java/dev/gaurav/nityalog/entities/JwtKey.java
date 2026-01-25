package dev.gaurav.nityalog.entities;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import dev.gaurav.nityalog.enums.JwtKeyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Entity
@Table(name = "jwt_key",
        indexes = {
                @Index(name = "idx_jwtkey_status_created", columnList = "status, created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class JwtKey extends BaseEntity {
    @Column(name = "algorithm", nullable = false, length = 20)
    private String algorithm; // e.g., RS256

    @Column(name = "public_key_pem", nullable = false, columnDefinition = "TEXT")
    private String publicKeyPem; // "-----BEGIN PUBLIC KEY-----..."

    @Column(name = "private_key_pem", nullable = false, columnDefinition = "TEXT")
    private String privateKeyPem; // "-----BEGIN PRIVATE KEY-----..."

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JwtKeyStatus status;

    public RSAKey toRsaJwk() {
        try {
            return new RSAKey.Builder((RSAPublicKey) this.toPublicKey())
                    .privateKey(this.toPrivateKey())
                    .keyID(this.getId().toString())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(new Algorithm(this.algorithm))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build RSA JWK", e);
        }
    }

    public PublicKey toPublicKey() {
        try {
            String pem = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid stored RSA public key", e);
        }
    }

    public PrivateKey toPrivateKey() {
        try {
            String pem = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid stored RSA private key", e);
        }
    }
}