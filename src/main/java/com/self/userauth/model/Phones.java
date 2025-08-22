package com.self.userauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.self.userauth.model.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "phones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phones extends BaseEntity {

	@Column(name = "phone", nullable = false)
	private String phone;

	@Column(name = "is_primary")
	private Boolean isPrimary;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	@JsonIgnore  // Avoid infinite recursion
	private User user;

	@Override
	protected String getPrefix() {
		return "phones";
	}
}
