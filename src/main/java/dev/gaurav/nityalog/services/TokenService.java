package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.entities.Token;
import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.models.TokenData;
import dev.gaurav.nityalog.repositories.TokenRepository;
import dev.gaurav.nityalog.utils.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository repository;

    public void save(User user, TokenData tokenData) {
        repository.save(Token.builder()
                .user(user)
                .tokenType(tokenData.tokenType())
                .hashedToken(HashUtils.hash(tokenData.token()))
                .expiresAt(Instant.now().plus(tokenData.expiresIn()))
                .jti(tokenData.jti())
                .build());
    }

    public void save(User user, List<TokenData> tokenDataList) {
        tokenDataList.forEach(tokenData -> save(user, tokenData));
    }
}
