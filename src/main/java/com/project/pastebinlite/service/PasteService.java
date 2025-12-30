package com.project.pastebinlite.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.pastebinlite.dto.CreatePasteResponse;
import com.project.pastebinlite.dto.PasteResponse;
import com.project.pastebinlite.dto.CreatePasteRequest;
import com.project.pastebinlite.entity.Paste;
import com.project.pastebinlite.exception.PasteNotFoundException;
import com.project.pastebinlite.repository.PasteRepository;

import java.time.LocalDateTime;

@Service
public class PasteService {

	@Autowired
	private PasteRepository pasteRepository;

	@Value("${TEST_MODE:0}")
	private String testMode;

	@Transactional
	public CreatePasteResponse createPaste(CreatePasteRequest request, String baseUrl) {
		// Create new paste entity
		Paste paste = new Paste();
		paste.setContent(request.getContent());

		// Set expiration time if ttl_seconds is provided
		if (request.getTtlSeconds() != null) {
			paste.setExpiresAt(LocalDateTime.now().plusSeconds(request.getTtlSeconds()));
		}

		// Set max views if provided
		if (request.getMaxViews() != null) {
			paste.setMaxViews(request.getMaxViews());
		}

		// Save paste (contentKey and createdAt will be auto-generated)
		Paste savedPaste = pasteRepository.save(paste);

		// Return response with id and URL
		return new CreatePasteResponse(savedPaste.getId(), baseUrl);
	}

	@Transactional
	public PasteResponse getPaste(String contentKey, String testNowMs) {
		// Get current time based on TEST_MODE and testNowMs
		LocalDateTime currentTime = getCurrentTime(testNowMs);

		// Find paste by contentKey
		Paste paste = pasteRepository.findById(contentKey)
				.orElseThrow(() -> new PasteNotFoundException("Paste not found"));

		// Check if paste has expired by time
		if (paste.getExpiresAt() != null && currentTime.isAfter(paste.getExpiresAt())) {
			throw new PasteNotFoundException("Paste has expired");
		}

		// Check if view limit exceeded
		if (paste.getMaxViews() != null && paste.getViewCount() >= paste.getMaxViews()) {
			throw new PasteNotFoundException("Paste has exceeded view limit");
		}

		// Increment view count
		paste.incrementViewCount();
		pasteRepository.save(paste);

		// Return response
		return new PasteResponse(paste.getContent(), paste.getRemainingViews(), paste.getExpiresAt());
	}

	private LocalDateTime getCurrentTime(String testNowMs) {
		// Only use testNowMs if TEST_MODE is enabled
		if ("1".equals(testMode) && testNowMs != null && !testNowMs.isEmpty()) {
			try {
				long millis = Long.parseLong(testNowMs);
				return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis),
						java.time.ZoneId.systemDefault());
			} catch (NumberFormatException e) {
				// Fall through to system time
			}
		}
		return LocalDateTime.now();
	}
}
