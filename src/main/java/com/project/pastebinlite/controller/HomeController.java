package com.project.pastebinlite.controller;

import com.project.pastebinlite.entity.User;
import com.project.pastebinlite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String home(Model model, Authentication auth) {
		// Check if user is authenticated
		if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
			String username = auth.getName();
			model.addAttribute("username", username);
			model.addAttribute("isAuthenticated", true);

			// Get all users for sharing dropdown (exclude current user)
			List<User> allUsers = userService.findAll();
			allUsers.removeIf(u -> u.getUsername().equals(username));
			model.addAttribute("users", allUsers);
		} else {
			model.addAttribute("isAuthenticated", false);
		}
		return "index";
	}
}
