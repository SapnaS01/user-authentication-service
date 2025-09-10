package com.self.userauth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.self.userauth.dto.UserDTO;
import com.self.userauth.model.User;
import com.self.userauth.pojo.ApiResponse;
import com.self.userauth.service.inter.UserServiceInter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserServiceInter userService;

	// get all users
	// TODO: dynamic filtering of fields 
	@GetMapping
	public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(Pageable pageable) {
		Page<UserDTO> users = userService.getAllUsers(pageable);
		return ResponseEntity.ok(
				new ApiResponse<>(true, "Fetched users successfully", users)
				);
	}


	// get user by ID
	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable String id) {
		return ResponseEntity.ok(userService.getUserById(id));
	}

	//    TODO
	// update user
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
		user.setId(id); // ensure the ID from path is used
		return ResponseEntity.ok(userService.updateUser(user));
	}

	//    TODO: add soft delete functionality
	//delete user 
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

}
