package com.self.userauth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.self.userauth.model.Phones;

public interface PhonesRepository extends JpaRepository<Phones, String> {
	boolean existsByPhone(String phone);

	Optional<Phones> findByPhone(String phone);

}
