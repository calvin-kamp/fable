package com.fable.backend.auth;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Validates that a JWT's {@code type} claim matches the expected token type.
 * <p>
 * Prevents refresh tokens from being accepted as access tokens and vice versa.
 */
public class TokenTypeValidator implements OAuth2TokenValidator<Jwt> {

	private final String expectedType;

	/**
	 * @param expectedType the required value of the {@code type} claim, either
	 *                     {@code access} or {@code refresh}
	 */
	public TokenTypeValidator(String expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		if (expectedType.equals(jwt.getClaimAsString("type"))) {
			return OAuth2TokenValidatorResult.success();
		}
		return OAuth2TokenValidatorResult.failure(
				new OAuth2Error("invalid_token", "Falscher Token-Typ.", null));
	}
}
