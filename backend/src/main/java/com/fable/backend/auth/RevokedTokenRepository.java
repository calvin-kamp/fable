package com.fable.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for revoked refresh tokens, keyed by the token's {@code jti}.
 */
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
}
