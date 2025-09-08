package com.self.userauth.mapper;

import com.self.userauth.dto.*;
import com.self.userauth.model.*;

import java.util.stream.Collectors;

public class UserMapper {

    public static UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImage(user.getProfileImage())
                .phone(user.getPhone() != null ? toPhoneDto(user.getPhone()) : null)
                .emails(user.getEmails() != null
                        ? user.getEmails().stream().map(UserMapper::toEmailDto).collect(Collectors.toList())
                        : null)
                .build();
    }

    public static User toEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setProfileImage(dto.getProfileImage());

        if (dto.getPhone() != null) {
            Phones phone = new Phones();
            phone.setId(dto.getPhone().getId());
            phone.setPhone(dto.getPhone().getPhone());
            phone.setIsPrimary(dto.getPhone().getIsPrimary());
            phone.setUser(user);
            user.setPhone(phone);
        }

        if (dto.getEmails() != null) {
            user.setEmails(dto.getEmails().stream().map(emailDTO -> {
                Emails email = new Emails();
                email.setId(emailDTO.getId());
                email.setEmail(emailDTO.getEmail());
                email.setIsPrimary(emailDTO.getIsPrimary());
                email.setUser(user);
                return email;
            }).collect(Collectors.toList()));
        }

        return user;
    }

    private static PhoneDTO toPhoneDto(Phones phone) {
        return PhoneDTO.builder()
                .id(phone.getId())
                .phone(phone.getPhone())
                .isPrimary(phone.getIsPrimary())
                .build();
    }

    private static EmailDTO toEmailDto(Emails email) {
        return EmailDTO.builder()
                .id(email.getId())
                .email(email.getEmail())
                .isPrimary(email.getIsPrimary())
                .build();
    }
}
