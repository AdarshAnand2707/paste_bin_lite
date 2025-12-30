package com.project.pastebinlite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasteResponse {

	private String content;
	
	@JsonProperty("remaining_views")
	private Integer remainingViews;
	
	@JsonProperty("expires_at")
	private LocalDateTime expiresAt;
}
