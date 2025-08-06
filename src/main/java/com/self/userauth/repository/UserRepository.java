package com.self.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.self.userauth.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
