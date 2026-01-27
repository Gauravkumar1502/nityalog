package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.enums.Role;
import dev.gaurav.nityalog.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(@NonNull String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by usernameOrEmail: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        log.info("User found with usernameOrEmail: {}", usernameOrEmail);
        return user;
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User createUser(String email) {
        User user = User.builder()
                .email(email)
                .username(this.generateUsernameFromEmail(email))
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    private String generateUsernameFromEmail(String email) {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        return email.substring(0, email.indexOf('@'))
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .concat("_")
                .concat(suffix);
    }
}
