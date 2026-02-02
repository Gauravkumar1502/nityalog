package dev.gaurav.nityalog.repositories;
    
import dev.gaurav.nityalog.entities.UserProvider;
import dev.gaurav.nityalog.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProviderRepository extends JpaRepository<UserProvider, UUID> {
    Optional<UserProvider> findByProviderTypeAndProviderUserId(ProviderType providerType, String providerUserId);
}
