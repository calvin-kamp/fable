package com.fable.backend.auth;

/**
 * Response body for login and token refresh.
 * <p>
 * The refresh token is not part of the body. It is sent as an HttpOnly cookie,
 * see {@link AuthController}.
 *
 * @param accessToken short-lived token for API requests
 * @param expiresIn   access token lifetime in seconds
 */
public record TokenResponse(String accessToken, long expiresIn) {
}
