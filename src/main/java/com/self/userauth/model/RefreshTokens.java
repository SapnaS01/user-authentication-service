package com.self.userauth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokens {

	@Id
	@Column(name = "id", nullable = false, updatable = false, unique = true,length = 100)
	private String id;

	@Column(name = "token", nullable = false, unique = true, length = 512)
	private String token;

	//    This means:
	//       A user can have many refresh tokens (one per device or session).
	//       A refresh token belongs to one user.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked", nullable = false)
	@Builder.Default
	private boolean revoked = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public boolean isExpired() {
		return expiresAt.isBefore(LocalDateTime.now());
	}

	public boolean isActive() {
		return !revoked && !isExpired();
	}

	@PrePersist
	protected void onCreate() {
		if (this.id == null || this.id.isBlank()) {
			String prefix = "refresh_tokens";
			this.id = prefix + "_" + UUID.randomUUID();
		}
	}
}
