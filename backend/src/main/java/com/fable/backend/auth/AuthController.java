package com.fable.backend.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fable.backend.user.User;
import com.fable.backend.user.UserRepository;
import com.fable.backend.user.UserResponse;

import jakarta.validation.Valid;

/**
 * REST endpoints for registration, login, token refresh and logout.
 * <p>
 * All endpoints under {@code /auth} are publicly accessible, see
 * {@link SecurityConfig}. The refresh token is exchanged exclusively via an
 * HttpOnly cookie so that it cannot be read by JavaScript.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private final UserRepository userRepository;
	private final RevokedTokenRepository revokedTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;

	AuthController(UserRepository userRepository, RevokedTokenRepository revokedTokenRepository,
			PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
			TokenService tokenService) {
		this.userRepository = userRepository;
		this.revokedTokenRepository = revokedTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
	}

	/**
	 * Registers a new user account.
	 *
	 * @param request the registration data
	 * @return the created user without password
	 * @throws ResponseStatusException with {@code 409 CONFLICT} if the email is
	 *                                 already taken
	 */
	@PostMapping("/registrierung")
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse register(@Valid @RequestBody RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-Mail ist bereits vergeben.");
		}

		User user = new User(
				request.email(),
				request.firstName(),
				request.lastName(),
				passwordEncoder.encode(request.password()));

		return UserResponse.from(userRepository.save(user));
	}

	/**
	 * Authenticates a user. Returns an access token in the body and sets the
	 * refresh token as an HttpOnly cookie.
	 *
	 * @param request the login credentials
	 * @return the access token and its lifetime
	 * @throws ResponseStatusException with {@code 401 UNAUTHORIZED} if the
	 *                                 credentials are invalid
	 */
	@PostMapping("/login")
	ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		try {
			authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
		} catch (AuthenticationException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-Mail oder Passwort falsch.");
		}

		// Authentication succeeded, so the user is guaranteed to exist.
		User user = userRepository.findByEmail(request.email()).orElseThrow();
		String refreshToken = tokenService.createRefreshToken(user);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie(refreshToken).toString())
				.body(tokenService.createAccessToken(user));
	}

	/**
	 * Issues a new access token for a valid, non-revoked refresh token cookie.
	 *
	 * @param refreshToken the refresh token from the cookie, if present
	 * @return a new access token and its lifetime
	 * @throws ResponseStatusException with {@code 401 UNAUTHORIZED} if the
	 *                                 cookie is missing or the refresh token is
	 *                                 invalid, expired or revoked
	 */
	@PostMapping("/refresh")
	TokenResponse refresh(
			@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
		if (refreshToken == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kein Refresh-Token vorhanden.");
		}

		Jwt jwt = decodeRefreshToken(refreshToken);

		if (revokedTokenRepository.existsById(jwt.getId())) {
			throw unauthorized();
		}

		User user = userRepository.findById(Long.valueOf(jwt.getSubject()))
				.orElseThrow(this::unauthorized);

		return tokenService.createAccessToken(user);
	}

	/**
	 * Revokes the refresh token and deletes the cookie.
	 * <p>
	 * Always succeeds, even if the cookie is missing or the token is already
	 * invalid, so the client ends up logged out in every case.
	 *
	 * @param refreshToken the refresh token from the cookie, if present
	 * @return an empty response that clears the cookie
	 */
	@PostMapping("/logout")
	ResponseEntity<Void> logout(
			@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
		if (refreshToken != null) {
			try {
				Jwt jwt = tokenService.decodeRefreshToken(refreshToken);
				revokedTokenRepository.save(new RevokedToken(jwt.getId(), jwt.getExpiresAt()));
			} catch (JwtException e) {
				// Invalid or expired tokens cannot be used anyway, nothing to revoke.
			}
		}

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, clearedRefreshTokenCookie().toString())
				.build();
	}

	/**
	 * Decodes a refresh token and maps any validation failure to {@code 401}.
	 */
	private Jwt decodeRefreshToken(String token) {
		try {
			return tokenService.decodeRefreshToken(token);
		} catch (JwtException e) {
			throw unauthorized();
		}
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültiges Refresh-Token.");
	}

	/**
	 * Builds the refresh token cookie. HttpOnly keeps it out of reach of
	 * JavaScript, SameSite=Strict protects against CSRF and the path limits it
	 * to the auth endpoints.
	 */
	private ResponseCookie refreshTokenCookie(String token) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
				.httpOnly(true)
				.secure(true)
				.sameSite("Strict")
				.path("/auth")
				.maxAge(tokenService.getRefreshTokenLifetime())
				.build();
	}

	/**
	 * Builds an expired cookie that makes the browser delete the refresh token.
	 */
	private ResponseCookie clearedRefreshTokenCookie() {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
				.httpOnly(true)
				.secure(true)
				.sameSite("Strict")
				.path("/auth")
				.maxAge(0)
				.build();
	}
}
