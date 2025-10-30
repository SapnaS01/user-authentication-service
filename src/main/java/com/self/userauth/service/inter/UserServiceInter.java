package com.self.userauth.service.inter;

import java.util.List;
import com.self.userauth.model.User;

public interface UserServiceInter {
	// Get user details by ID
	User getUserById(String userId);


	// Update user details (name, email, etc.)
	User updateUser(User user);

	// Delete a user
	void deleteUser(String userId);

	// Get all users (for admin purposes) add pagination support 
	List<User> getAllUsers();
}
