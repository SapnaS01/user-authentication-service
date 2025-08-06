package com.self.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.self.userauth.model.Emails;

public interface EmailsRepository extends JpaRepository<Emails, String> {

}
