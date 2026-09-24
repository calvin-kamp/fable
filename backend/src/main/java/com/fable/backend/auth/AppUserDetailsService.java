package com.fable.backend.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fable.backend.user.UserRepository;

/**
 * Loads users from the database for Spring Security's authentication process.
 * <p>
 * Used by the {@link org.springframework.security.authentication.AuthenticationManager}
 * during login only. Subsequent requests are authenticated via JWT and do not
 * hit this service.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

	private final UserRepository repository;

	public AppUserDetailsService(UserRepository repository) {
		this.repository = repository;
	}

	/**
	 * Loads a user by email address.
	 *
	 * @param email the email address entered at login
	 * @return the user's credentials and authorities
	 * @throws UsernameNotFoundException if no account exists for the given email
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return repository.findByEmail(email)
				.map(user -> User.withUsername(user.getEmail())
						.password(user.getPassword())
						.roles("USER")
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Kein Konto mit dieser E-Mail."));
	}
}
