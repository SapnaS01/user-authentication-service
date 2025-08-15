package com.self.userauth.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {

    @NotBlank(message = "First Name is required")
    private String firstName;
    
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

}
