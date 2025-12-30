package com.project.pastebinlite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.pastebinlite.dto.CreatePasteRequest;
import com.project.pastebinlite.dto.CreatePasteResponse;
import com.project.pastebinlite.dto.PasteResponse;
import com.project.pastebinlite.service.PasteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pastes")
public class PasteApiController {

	@Autowired
	private PasteService pasteService;

	@PostMapping
	public ResponseEntity<CreatePasteResponse> createPaste(
			@Valid @RequestBody CreatePasteRequest request,
			HttpServletRequest httpRequest) {
		String baseUrl = getBaseUrl(httpRequest);
		CreatePasteResponse response = pasteService.createPaste(request, baseUrl);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PasteResponse> getPaste(@PathVariable String id,
			@RequestHeader(value = "x-test-now-ms", required = false) String testNowMs) {
		PasteResponse response = pasteService.getPaste(id, testNowMs);
		return ResponseEntity.ok(response);
	}

	private String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		
		StringBuilder baseUrl = new StringBuilder();
		baseUrl.append(scheme).append("://").append(serverName);
		
		// Only include port if it's not the default port
		if (("http".equals(scheme) && serverPort != 80) || 
			("https".equals(scheme) && serverPort != 443)) {
			baseUrl.append(":").append(serverPort);
		}
		
		return baseUrl.toString();
	}
}
