package dev.gaurav.nityalog.repositories;

import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {

    long countByTargetAndOtpTypeAndCreatedAtAfter(String target, OtpType otpType, Instant from);

    Optional<Otp> findTopByTargetAndOtpTypeOrderByCreatedAtDesc(String target, OtpType otpType);

    Optional<Otp> findTopByTargetAndOtpTypeAndUsedAtIsNotNullAndRevokedAtIsNullOrderByUsedAtDesc(String target, OtpType otpType);

    @Query("""
        SELECT MAX(o.usedAt)
        FROM Otp o
        WHERE o.target = :target
          AND o.otpType = :otpType
          AND o.usedAt IS NOT NULL
          AND o.revokedAt IS NULL
    """)
    Optional<Instant> findLastSuccessfulUsedAt(@Param("target") String target, @Param("otpType") OtpType otpType);

    @Query("""
        SELECT SUM(o.attemptCount)
        FROM Otp o
        WHERE o.target = :target
          AND o.otpType = :otpType
          AND o.createdAt >= :fromTime
    """)
    Long countFailedAttempts(@Param("target") String target, @Param("otpType") OtpType otpType, @Param("fromTime") Instant fromTime);

}
