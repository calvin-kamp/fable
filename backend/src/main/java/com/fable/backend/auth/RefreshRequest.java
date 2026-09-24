package com.fable.backend.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /auth/refresh} and {@code POST /auth/logout}.
 *
 * @param refreshToken the refresh token issued at login
 */
public record RefreshRequest(
		@NotBlank(message = "Refresh-Token darf nicht leer sein.") String refreshToken) {
}
