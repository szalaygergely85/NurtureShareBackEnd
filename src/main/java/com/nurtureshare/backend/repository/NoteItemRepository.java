package com.nurtureshare.backend.repository;

import com.nurtureshare.backend.model.NoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteItemRepository extends JpaRepository<NoteItem, UUID> {
    List<NoteItem> findByNoteIdOrderByOrderIndex(UUID noteId);
}
