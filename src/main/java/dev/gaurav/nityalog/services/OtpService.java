package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.entities.Otp;
import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.repositories.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {
    private final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final OtpRepository otpRepository;
    private final SecureRandom RANDOM = new SecureRandom();

    public String generateOtpCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

    public String generateOtpCode() {
        return generateOtpCode(8);
    }

    public Otp save(Otp otp) {
        return otpRepository.save(otp);
    }

    public long countRecentOtps(String target, OtpType otpType, Duration duration) {
        return otpRepository.countByTargetAndOtpTypeAndCreatedAtAfter(target, otpType, Instant.now().minus(duration));
    }

    public Optional<Otp> getLatestOtp(String target, OtpType otpType) {
        return otpRepository.findTopByTargetAndOtpTypeOrderByCreatedAtDesc(target, otpType);
    }

    public Optional<Instant> getLastSuccessfulOtpUseTime(String target, OtpType otpType) {
        return otpRepository.findLastSuccessfulUsedAt(target, otpType);
    }

}
