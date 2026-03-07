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
@Table(name = "users")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "sharedPastes")
@ToString(exclude = "sharedPastes")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private Boolean enabled = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@ManyToMany(mappedBy = "sharedWith")
	private Set<Paste> sharedPastes = new HashSet<>();

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
