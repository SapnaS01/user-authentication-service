package com.self.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.self.userauth.model.OtpCache;

public interface OtpCacheRepository extends JpaRepository<OtpCache, String> {

}
