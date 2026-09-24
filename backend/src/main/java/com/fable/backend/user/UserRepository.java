package com.fable.backend.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link User} entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * @param email the email address to search for
	 * @return the matching user, or an empty {@link Optional} if none exists
	 */
	Optional<User> findByEmail(String email);

	/**
	 * @param email the email address to check
	 * @return {@code true} if an account with this email already exists
	 */
	boolean existsByEmail(String email);

}
