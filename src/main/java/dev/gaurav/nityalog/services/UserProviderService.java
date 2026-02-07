package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.entities.UserProvider;
import dev.gaurav.nityalog.enums.ProviderType;
import dev.gaurav.nityalog.repositories.UserProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProviderService {
    private final UserProviderRepository userProviderRepository;

    public UserProvider buildProvider(User user, ProviderType type, String username, String providerUserId, String email) {
        return UserProvider.builder()
                .user(user)
                .providerType(type)
                .providerUsername(username)
                .providerUserId(providerUserId)
                .providerEmail(email)
                .build();
    }
}
