package com.project.pastebinlite.service;

import com.project.pastebinlite.dto.RegisterRequest;
import com.project.pastebinlite.entity.User;
import com.project.pastebinlite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public User registerUser(RegisterRequest request) {
		// Check if username already exists
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}

		// Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}

		// Create new user
		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEnabled(true);

		return userRepository.save(user);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	public List<User> findUsersByUsernames(List<String> usernames) {
		return usernames.stream()
				.map(userRepository::findByUsername)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
	}
}
