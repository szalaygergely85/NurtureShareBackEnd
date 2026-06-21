package com.nurtureshare.backend.repository;

import com.nurtureshare.backend.model.Note;
import com.nurtureshare.backend.model.enums.NoteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByCoupleId(UUID coupleId);
    List<Note> findByCoupleIdAndCategory(UUID coupleId, NoteCategory category);
}
