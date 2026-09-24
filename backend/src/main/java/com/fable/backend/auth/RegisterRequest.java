package com.fable.backend.auth;

import java.util.Objects;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /auth/registrierung}.
 * <p>
 * The password length is capped at 72 characters because BCrypt ignores
 * everything beyond 72 bytes.
 *
 * @param email           the user's email address, must be unique
 * @param firstName       the user's first name
 * @param lastName        the user's last name, optional
 * @param password        the plain-text password
 * @param passwordConfirm must match {@code password}, not persisted
 */
public record RegisterRequest(
		@NotBlank(message = "E-Mail darf nicht leer sein.")
		@Email(message = "E-Mail-Adresse ist ungültig.")
		String email,

		@NotBlank(message = "Vorname darf nicht leer sein.")
		String firstName,

		String lastName,

		@NotBlank(message = "Passwort darf nicht leer sein.")
		@Size(min = 8, max = 72, message = "Passwort muss zwischen 8 und 72 Zeichen lang sein.")
		String password,

		@NotBlank(message = "Passwort-Bestätigung darf nicht leer sein.")
		@Size(min = 8, max = 72, message = "Passwort-Bestätigung muss zwischen 8 und 72 Zeichen lang sein.")
		String passwordConfirm) {

	/**
	 * Checks that both password fields match.
	 * <p>
	 * Picked up by Bean Validation because the method follows the getter
	 * naming convention ({@code is...}).
	 */
	@AssertTrue(message = "Passwörter müssen übereinstimmen.")
	public boolean isPasswordMatching() {
		return Objects.equals(password, passwordConfirm);
	}
}
