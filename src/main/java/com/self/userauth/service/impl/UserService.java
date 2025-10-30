package com.self.userauth.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

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
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

}
