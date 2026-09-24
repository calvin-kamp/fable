package com.fable.backend.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /auth/login}.
 *
 * @param email    the user's email address
 * @param password the user's plain-text password
 */
public record LoginRequest(
		@NotBlank(message = "E-Mail darf nicht leer sein.") String email,
		@NotBlank(message = "Passwort darf nicht leer sein.") String password) {
}
