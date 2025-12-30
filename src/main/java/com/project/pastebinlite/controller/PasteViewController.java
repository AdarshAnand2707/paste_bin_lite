package com.project.pastebinlite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.pastebinlite.dto.PasteResponse;
import com.project.pastebinlite.exception.PasteNotFoundException;
import com.project.pastebinlite.service.PasteService;

@Controller
@RequestMapping("/p")
public class PasteViewController {

	@Autowired
	private PasteService pasteService;

	@GetMapping("/{id}")
	public String viewPaste(@PathVariable String id,
			@RequestHeader(value = "x-test-now-ms", required = false) String testNowMs,
			Model model) {
		try {
			PasteResponse response = pasteService.getPaste(id, testNowMs);
			model.addAttribute("content", response.getContent());
			model.addAttribute("remainingViews", response.getRemainingViews());
			model.addAttribute("expiresAt", response.getExpiresAt());
			return "view-paste";
		} catch (PasteNotFoundException e) {
			model.addAttribute("message", e.getMessage());
			return "404";
		}
	}
}
