package com.project.pastebinlite.controller;

import com.project.pastebinlite.entity.Paste;
import com.project.pastebinlite.service.PasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

	@Autowired
	private PasteService pasteService;

	@GetMapping("/dashboard")
	public String dashboard(Model model, Authentication auth) {
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
			return "redirect:/login";
		}

		String username = auth.getName();
		model.addAttribute("username", username);

		// Get pastes owned by the user
		List<Paste> ownedPastes = pasteService.getUserOwnedPastes(username);
		model.addAttribute("ownedPastes", ownedPastes);

		// Get pastes shared with the user
		List<Paste> sharedPastes = pasteService.getUserSharedPastes(username);
		model.addAttribute("sharedPastes", sharedPastes);

		return "dashboard";
	}
}
