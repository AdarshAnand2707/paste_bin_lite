package com.project.pastebinlite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Paste {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;
	
	@Column(columnDefinition = "MEDIUMTEXT", nullable = false)
	private String content;
	
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column
	private LocalDateTime expiresAt;
	
	@Column
	private Integer maxViews;
	
	@Column(nullable = false)
	private Integer viewCount = 0;
	
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
	
	public void incrementViewCount() {
        this.viewCount++;
    }
	
	public Integer getRemainingViews() {
        if (maxViews == null) {
            return null;
        }
        return Math.max(0, maxViews - viewCount);
    }

    public boolean isExpired(LocalDateTime currentTime) {
        if (expiresAt != null && currentTime.isAfter(expiresAt)) {
            return true;
        }
        if (maxViews != null && viewCount >= maxViews) {
            return true;
        }
        return false;
    }

}
