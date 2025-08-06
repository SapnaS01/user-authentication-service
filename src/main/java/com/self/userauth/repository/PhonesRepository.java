package com.self.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.self.userauth.model.Phones;

public interface PhonesRepository extends JpaRepository<Phones, String> {
	boolean existsByPhone(String phone);

	Phones findByPhone(String phone);

}
