package com.fable.backend.user;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

/**
 * A registered user account.
 * <p>
 * The table is named {@code users} because {@code user} is a reserved word in
 * PostgreSQL. The password is always stored as a BCrypt hash.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Email(message = "E-Mail-Adresse ist ungültig.")
	private String email;

	private String firstName;
	private String lastName;

	/** BCrypt hash, never the plain-text password. */
	private String password;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	/** Required by JPA. */
	public User() {
	}

	/**
	 * @param email     the user's email address
	 * @param firstName the user's first name
	 * @param lastName  the user's last name, may be {@code null}
	 * @param password  the already hashed password
	 */
	public User(String email, String firstName, String lastName, String password) {
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPassword() {
		return password;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
