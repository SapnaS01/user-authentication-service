package com.self.userauth.dto;

import lombok.Data;

@Data
public class VerifyOtpDto {
    private String phone;
    private String otp;
}
