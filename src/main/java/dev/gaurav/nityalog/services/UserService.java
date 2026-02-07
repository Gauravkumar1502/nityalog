package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.entities.User;
import dev.gaurav.nityalog.entities.UserProvider;
import dev.gaurav.nityalog.enums.OtpType;
import dev.gaurav.nityalog.enums.Role;
import dev.gaurav.nityalog.exceptions.UserNotFoundException;
import dev.gaurav.nityalog.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
                .orElseThrow(() -> new UserNotFoundException("No user exists with username or email: " + usernameOrEmail));
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

    public User buildUser(String identifier, OtpType otpType) {
        boolean isEmail = otpType.equals(OtpType.EMAIL_VERIFY);
        User drafUser = User.builder()
            .email(isEmail ? identifier : null)
            .username(isEmail ? this.generateUsernameFromEmail(identifier) : identifier)
            .role(Role.USER)
            .build();
        if (!isEmail) {
            drafUser.getProfile().setPhoneNumber(identifier);
        }
        return drafUser;
    }

    public String generateUsernameFromEmail(String email) {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        return email.substring(0, email.indexOf('@'))
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .concat("_")
                .concat(suffix);
    }

    public void validateUserStatus(User user) {
        if (!user.isEnabled()) {
            throw new DisabledException("User account is disabled.");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("User account is locked.");
        }

        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired.");
        }

        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired.");
        }
    }

    public User save(User newUser) {
        return userRepository.save(newUser);
    }

    public User saveWithProvider(User user, UserProvider provider) {
        User savedUser = userRepository.save(user);
        provider.setUser(savedUser);
        savedUser.addProvider(provider);
        return userRepository.save(savedUser);
    }

    public User createOAuthUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .role(Role.USER)
                .build();
    }
}
