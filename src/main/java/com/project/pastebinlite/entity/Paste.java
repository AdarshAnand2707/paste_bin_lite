package com.project.pastebinlite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"owner", "sharedWith"})
@ToString(exclude = {"owner", "sharedWith"})
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id")
	private User owner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Visibility visibility = Visibility.PUBLIC;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "paste_shares",
		joinColumns = @JoinColumn(name = "paste_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private Set<User> sharedWith = new HashSet<>();
	
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
