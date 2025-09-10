package com.self.userauth.service.impl;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.self.userauth.dto.UserDTO;
import com.self.userauth.mapper.UserMapper;
import com.self.userauth.model.User;
import com.self.userauth.repository.UserRepository;
import com.self.userauth.service.inter.UserServiceInter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInter {

	private final UserRepository userRepository;

	@Override
	public User getUserById(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	}

	@Override
	public User updateUser(User user) {
		if (!userRepository.existsById(user.getId())) {
			throw new RuntimeException("User not found with ID: " + user.getId());
		}
		return userRepository.save(user); // saves updated user
	}

	@Override
	public void deleteUser(String userId) {
		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("User not found with ID: " + userId);
		}
		userRepository.deleteById(userId);
	}

	@Override
	public Page<UserDTO> getAllUsers(Pageable pageable){
		Page<User> users= userRepository.findAll(pageable);
		return new PageImpl<>(
				users.getContent().stream()
				.map(UserMapper::toDto)
				.collect(Collectors.toList()),
				pageable,
				users.getTotalElements()
				);

	}	

}
