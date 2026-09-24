package com.fable.backend.user;

import java.time.Instant;

/**
 * Public representation of a {@link User}, excluding the password.
 *
 * @param id        the user's ID
 * @param email     the user's email address
 * @param firstName the user's first name
 * @param lastName  the user's last name, may be {@code null}
 * @param createdAt the time the account was created
 */
public record UserResponse(Long id, String email, String firstName, String lastName, Instant createdAt) {

	/**
	 * Maps an entity to its response representation.
	 */
	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getCreatedAt());
	}
}
