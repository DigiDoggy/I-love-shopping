package shop.app.auth.repository;

import shop.app.auth.model.OauthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OauthAccountRepository extends JpaRepository<OauthAccount, UUID> {
    Optional<OauthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
