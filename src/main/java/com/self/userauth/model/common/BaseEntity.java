package com.self.userauth.model.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

// (DRY) Don't Repeat Yourself principle is applied here to avoid code duplication
@MappedSuperclass // This is a parent class with common fields and logic
@Getter
@Setter
public abstract class BaseEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false, unique = true)
	private String id;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	/**
	 * Auto-set id with table-based prefix and timestamps
	 */
	@PrePersist
	protected void onCreate() {
		if (this.id == null || this.id.isBlank()) {
			String prefix = getPrefix();
			this.id = prefix + "_" + UUID.randomUUID();
		}
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * This method should be overridden by each entity to return its own prefix.
	 */
	protected abstract String getPrefix();
}
