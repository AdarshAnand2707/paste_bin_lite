package com.project.pastebinlite.controller;

import com.project.pastebinlite.dto.RegisterRequest;
import com.project.pastebinlite.entity.User;
import com.project.pastebinlite.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@GetMapping("/login")
	public String loginPage(Authentication auth) {
		// Redirect to home if already logged in
		if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
			return "redirect:/";
		}
		return "login";
	}

	@GetMapping("/register")
	public String registerPage(Model model, Authentication auth) {
		// Redirect to home if already logged in
		if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
			return "redirect:/";
		}
		model.addAttribute("registerRequest", new RegisterRequest());
		return "register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute RegisterRequest request,
						   BindingResult result,
						   RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "register";
		}

		try {
			User user = userService.registerUser(request);
			redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
			return "redirect:/login";
		} catch (IllegalArgumentException e) {
			result.rejectValue(null, "error.registerRequest", e.getMessage());
			return "register";
		}
	}
}
