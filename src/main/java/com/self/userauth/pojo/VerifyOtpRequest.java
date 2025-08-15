package com.self.userauth.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
	@NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
	private String phone;
	
	@NotBlank(message = "OTP is required")
	@Size(min = 4, max = 4, message = "OTP must be exactly 4 digits")
	private String otp;

}
