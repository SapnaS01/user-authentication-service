package com.self.userauth.model;

import java.time.LocalDateTime;

import com.self.userauth.model.common.BaseEntity;
import com.self.userauth.model.enums.OtpAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "otp_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCache extends BaseEntity {

	@Column(name = "otp", nullable = false, length = 4)
	private String otp;

	@Column(name = "phone", nullable = false, length = 10)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, columnDefinition = "ENUM('SIGNUP', 'LOGIN', 'REGISTER')")
	private OtpAction action;
	
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;  
	

	@Override
	protected String getPrefix() {
		return "otp_cache";
	}
}
