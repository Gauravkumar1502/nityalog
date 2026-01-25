package dev.gaurav.nityalog.repositories;

import dev.gaurav.nityalog.entities.JwtKey;
import dev.gaurav.nityalog.enums.JwtKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JwtKeyRepository extends JpaRepository<JwtKey, UUID> {
    Optional<JwtKey> findFirstByStatusOrderByCreatedAtDesc(JwtKeyStatus status);

    List<JwtKey> findByStatusInOrderByCreatedAtDesc(List<JwtKeyStatus> statuses);

    @Modifying
    @Query("UPDATE JwtKey k SET k.status = 'RETIRED' WHERE k.status = 'ACTIVE'")
    int retireAllActiveKeys();

    Optional<JwtKey> findById(Long id);

    List<JwtKey> findByStatus(JwtKeyStatus status);

    Optional<JwtKey> findByIdAndStatusIn(UUID keyId, List<JwtKeyStatus> statuses);

}
