package com.fable.backend.auth;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A refresh token that has been revoked on logout.
 * <p>
 * Identified by the token's {@code jti} claim. The expiry date is stored so
 * that entries can be cleaned up once the token would have expired anyway.
 */
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

	@Id
	private String jti;

	private Instant expiresAt;

	/** Required by JPA. */
	protected RevokedToken() {
	}

	public RevokedToken(String jti, Instant expiresAt) {
		this.jti = jti;
		this.expiresAt = expiresAt;
	}

	public String getJti() {
		return jti;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
