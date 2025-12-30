package com.project.pastebinlite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePasteRequest {

	@NotBlank(message = "Content is required and must be non-empty")
	@Size(max = 16777216, message = "Content must not exceed 16MB")
	private String content;

	@JsonProperty("ttl_seconds")
	@Min(value = 1, message = "ttl_seconds must be at least 1")
	private Integer ttlSeconds;

	@JsonProperty("max_views")
	@Min(value = 1, message = "max_views must be at least 1")
	private Integer maxViews;
}
