package dev.gaurav.nityalog.repositories;

import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.OtpType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {

    long countByTargetAndOtpTypeAndCreatedAtAfter(String target, OtpType otpType, Instant from);

    long countByUserAndOtpTypeAndCreatedAtAfter(User user, OtpType otpType, Instant from);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Otp> findTopByTargetAndOtpTypeOrderByCreatedAtDesc(String target, OtpType otpType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Otp> findTopByUserAndOtpTypeOrderByCreatedAtDesc(User user, OtpType otpType);

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
        SELECT MAX(o.usedAt)
        FROM Otp o
        WHERE o.user = :user
          AND o.otpType = :otpType
          AND o.usedAt IS NOT NULL
          AND o.revokedAt IS NULL
    """)
    Optional<Instant> findLastSuccessfulUsedAt(@Param("user") User user, @Param("otpType") OtpType otpType);

    @Query("""
        SELECT COALESCE(SUM(o.attemptCount), 0)
        FROM Otp o
        WHERE o.target = :target
          AND o.otpType = :otpType
          AND o.createdAt >= :from
    """)
    long countFailedAttempts(@Param("target") String target, @Param("otpType") OtpType otpType, @Param("from") Instant from);

    @Query("""
        SELECT COALESCE(SUM(o.attemptCount), 0)
        FROM Otp o
        WHERE o.user = :user
          AND o.otpType = :otpType
          AND o.createdAt >= :from
    """)
    long countFailedAttempts(@Param("user") User user, @Param("otpType") OtpType otpType, @Param("from") Instant from);

}
