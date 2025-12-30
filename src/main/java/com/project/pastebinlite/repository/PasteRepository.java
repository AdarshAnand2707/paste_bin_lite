package com.project.pastebinlite.repository;

import org.springframework.data.repository.CrudRepository;

import com.project.pastebinlite.entity.Paste;

public interface PasteRepository extends CrudRepository<Paste, String> {

}
