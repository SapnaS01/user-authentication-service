package com.self.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.self.userauth.model.RefreshTokens;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, String> {

}
