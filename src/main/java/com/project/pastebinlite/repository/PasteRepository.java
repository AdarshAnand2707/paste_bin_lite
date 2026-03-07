package com.project.pastebinlite.repository;

import org.springframework.data.repository.CrudRepository;

import com.project.pastebinlite.entity.Paste;
import com.project.pastebinlite.entity.User;

import java.util.List;

public interface PasteRepository extends CrudRepository<Paste, String> {

	List<Paste> findByOwnerOrderByCreatedAtDesc(User owner);

	List<Paste> findBySharedWithContainingOrderByCreatedAtDesc(User user);

}
