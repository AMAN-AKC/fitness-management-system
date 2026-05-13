package com.fitness.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long tokenId;

	@Column(nullable = false, unique = true, length = 100)
	private String token;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private SystemUser user;

	@Column(nullable = false)
	private LocalDateTime expiryDate;

	@Column(nullable = false)
	@Builder.Default
	private Boolean used = false;
}
