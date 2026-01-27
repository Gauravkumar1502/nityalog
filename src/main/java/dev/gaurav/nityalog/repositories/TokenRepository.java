package dev.gaurav.nityalog.repositories;

import dev.gaurav.nityalog.entities.Token;
import dev.gaurav.nityalog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByJti(String jti);
    Optional<Token> findByHashedToken(String hashedToken);
    List<Token> findAllByUser(User user);
    List<Token> findAllByUserId(UUID userId);
    long deleteByUserIdAndExpiresAtBefore(UUID userId, Instant before);
}
