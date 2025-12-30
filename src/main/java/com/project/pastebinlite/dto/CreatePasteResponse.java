package com.project.pastebinlite.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatePasteResponse {

	private String id;
	private String url;
	
	public CreatePasteResponse(String id, String baseUrl) {
		this.id = id;
		this.url = baseUrl + "/p/" + id;
	}
}
