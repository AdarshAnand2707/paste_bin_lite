package com.project.pastebinlite.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.pastebinlite.dto.CreatePasteResponse;
import com.project.pastebinlite.dto.PasteResponse;
import com.project.pastebinlite.dto.CreatePasteRequest;
import com.project.pastebinlite.entity.Paste;
import com.project.pastebinlite.entity.User;
import com.project.pastebinlite.entity.Visibility;
import com.project.pastebinlite.exception.PasteNotFoundException;
import com.project.pastebinlite.repository.PasteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class PasteService {

	@Autowired
	private PasteRepository pasteRepository;

	@Autowired
	private UserService userService;

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

		// Set owner if user is authenticated
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
			String username = auth.getName();
			User owner = userService.findByUsername(username)
					.orElseThrow(() -> new IllegalArgumentException("User not found"));
			paste.setOwner(owner);
		}

		// Set visibility (default to PUBLIC if not specified or user is not authenticated)
		if (request.getVisibility() != null && paste.getOwner() != null) {
			try {
				paste.setVisibility(Visibility.valueOf(request.getVisibility().toUpperCase()));
			} catch (IllegalArgumentException e) {
				paste.setVisibility(Visibility.PUBLIC);
			}
		} else {
			paste.setVisibility(Visibility.PUBLIC);
		}

		// Add shared users if specified and paste is PRIVATE
		if (paste.getVisibility() == Visibility.PRIVATE &&
			request.getSharedUsernames() != null &&
			!request.getSharedUsernames().isEmpty()) {
			List<User> sharedUsers = userService.findUsersByUsernames(request.getSharedUsernames());
			paste.setSharedWith(new HashSet<>(sharedUsers));
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

		// Check access permissions for private pastes
		if (paste.getVisibility() == Visibility.PRIVATE) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String currentUsername = (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser"))
					? auth.getName()
					: null;

			// Check if user is owner or in shared list
			boolean hasAccess = false;
			if (currentUsername != null) {
				// Check if current user is the owner
				if (paste.getOwner() != null && paste.getOwner().getUsername().equals(currentUsername)) {
					hasAccess = true;
					System.out.println("DEBUG: User " + currentUsername + " is the owner");
				}
				// Check if current user is in the shared list
				else if (paste.getSharedWith() != null && !paste.getSharedWith().isEmpty()) {
					System.out.println("DEBUG: Checking shared users. Count: " + paste.getSharedWith().size());
					for (User sharedUser : paste.getSharedWith()) {
						System.out.println("DEBUG: Shared with: " + sharedUser.getUsername());
					}
					hasAccess = paste.getSharedWith().stream()
							.anyMatch(user -> user.getUsername().equals(currentUsername));
					if (hasAccess) {
						System.out.println("DEBUG: User " + currentUsername + " found in shared list");
					} else {
						System.out.println("DEBUG: User " + currentUsername + " NOT found in shared list");
					}
				} else {
					System.out.println("DEBUG: No shared users found for this paste");
				}
			} else {
				System.out.println("DEBUG: No authenticated user (anonymous access attempt)");
			}

			if (!hasAccess) {
				System.out.println("DEBUG: Access denied for user: " + currentUsername);
				throw new AccessDeniedException("You do not have permission to view this paste");
			}
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

	public List<Paste> getUserOwnedPastes(String username) {
		User user = userService.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return pasteRepository.findByOwnerOrderByCreatedAtDesc(user);
	}

	public List<Paste> getUserSharedPastes(String username) {
		User user = userService.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return pasteRepository.findBySharedWithContainingOrderByCreatedAtDesc(user);
	}
}
