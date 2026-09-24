package com.fable.backend.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.fable.backend.user.User;

/**
 * Creates and validates access and refresh tokens.
 * <p>
 * Both token types carry a {@code type} claim ({@code access} or
 * {@code refresh}) so that one cannot be used in place of the other.
 */
@Service
public class TokenService {

	private static final String ISSUER = "fable";
	private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
	private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(7);

	private final JwtEncoder encoder;

	/**
	 * Dedicated decoder for refresh tokens. Deliberately not a bean, as it would
	 * otherwise conflict with the access token decoder in {@link SecurityConfig}.
	 */
	private final NimbusJwtDecoder refreshDecoder;

	public TokenService(JwtEncoder encoder, SecretKey jwtSecretKey) {
		this.encoder = encoder;
		this.refreshDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		this.refreshDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefault(),
				new TokenTypeValidator("refresh")));
	}

	/**
	 * Creates a new access token.
	 *
	 * @param user the authenticated user
	 * @return the access token and its lifetime
	 */
	public TokenResponse createAccessToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.issuedAt(now)
				.expiresAt(now.plus(ACCESS_TOKEN_LIFETIME))
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("type", "access")
				.build();
		return new TokenResponse(encode(claims), ACCESS_TOKEN_LIFETIME.toSeconds());
	}

	/**
	 * Creates a new refresh token.
	 *
	 * @param user the authenticated user
	 * @return the encoded refresh token
	 */
	public String createRefreshToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.issuedAt(now)
				.expiresAt(now.plus(REFRESH_TOKEN_LIFETIME))
				.subject(user.getId().toString())
				// Unique ID (jti) used to revoke the token on logout.
				.id(UUID.randomUUID().toString())
				.claim("type", "refresh")
				.build();
		return encode(claims);
	}

	/**
	 * @return how long a refresh token stays valid, used as the cookie's max age
	 */
	public Duration getRefreshTokenLifetime() {
		return REFRESH_TOKEN_LIFETIME;
	}

	/**
	 * Decodes and validates a refresh token.
	 *
	 * @param token the encoded refresh token
	 * @return the decoded token
	 * @throws JwtException if the token is invalid, expired or not a refresh
	 *                      token
	 */
	public Jwt decodeRefreshToken(String token) {
		return refreshDecoder.decode(token);
	}

	private String encode(JwtClaimsSet claims) {
		// HS256 must be set explicitly. Without it, the encoder defaults to RS256
		// and fails because no RSA key is configured.
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
