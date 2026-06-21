package com.nurtureshare.backend.service;

import com.nurtureshare.backend.dto.request.CreateNoteItemRequest;
import com.nurtureshare.backend.dto.request.CreateNoteRequest;
import com.nurtureshare.backend.dto.request.UpdateNoteItemRequest;
import com.nurtureshare.backend.dto.response.NoteItemResponse;
import com.nurtureshare.backend.dto.response.NoteResponse;
import com.nurtureshare.backend.dto.response.UserResponse;
import com.nurtureshare.backend.exception.ResourceNotFoundException;
import com.nurtureshare.backend.exception.UnauthorizedException;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.Note;
import com.nurtureshare.backend.model.NoteItem;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.model.enums.NoteCategory;
import com.nurtureshare.backend.repository.NoteItemRepository;
import com.nurtureshare.backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final CoupleService coupleService;
    private final NoteRepository noteRepository;
    private final NoteItemRepository noteItemRepository;

    public List<NoteResponse> getNotes(User currentUser, String category) {
        Couple couple = coupleService.getCoupleForUser(currentUser);
        UUID coupleId = couple.getId();

        List<Note> notes;
        if (category == null || category.isBlank()) {
            notes = noteRepository.findByCoupleId(coupleId);
        } else {
            NoteCategory noteCategory;
            try {
                noteCategory = NoteCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid category: " + category);
            }
            notes = noteRepository.findByCoupleIdAndCategory(coupleId, noteCategory);
        }

        return notes.stream().map(this::toNoteResponse).collect(Collectors.toList());
    }

    public NoteResponse createNote(User currentUser, CreateNoteRequest req) {
        Couple couple = coupleService.getCoupleForUser(currentUser);

        Note note = Note.builder()
                .couple(couple)
                .createdBy(currentUser)
                .title(req.getTitle())
                .category(req.getCategory())
                .syncedWithPartner(req.isSyncedWithPartner())
                .build();
        note = noteRepository.save(note);

        return toNoteResponse(note);
    }

    public void deleteNote(User currentUser, UUID noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        if (!note.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this note.");
        }

        noteRepository.delete(note);
    }

    public NoteItemResponse addNoteItem(User currentUser, UUID noteId, CreateNoteItemRequest req) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        if (!note.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to add items to this note.");
        }

        NoteItem item = NoteItem.builder()
                .note(note)
                .content(req.getContent())
                .itemType(req.getItemType())
                .urgent(req.isUrgent())
                .orderIndex(req.getOrderIndex())
                .build();
        item = noteItemRepository.save(item);

        return toNoteItemResponse(item);
    }

    public NoteItemResponse updateNoteItem(User currentUser, UUID noteId, UUID itemId, UpdateNoteItemRequest req) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        if (!note.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update items in this note.");
        }

        NoteItem item = noteItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Note item not found with id: " + itemId));

        if (!item.getNote().getId().equals(noteId)) {
            throw new IllegalArgumentException("Note item does not belong to the specified note.");
        }

        if (req.getContent() != null) item.setContent(req.getContent());
        if (req.getItemType() != null) item.setItemType(req.getItemType());
        if (req.getChecked() != null) item.setChecked(req.getChecked());
        if (req.getUrgent() != null) item.setUrgent(req.getUrgent());
        if (req.getOrderIndex() != null) item.setOrderIndex(req.getOrderIndex());

        item = noteItemRepository.save(item);
        return toNoteItemResponse(item);
    }

    public void deleteNoteItem(User currentUser, UUID noteId, UUID itemId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        if (!note.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete items from this note.");
        }

        NoteItem item = noteItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Note item not found with id: " + itemId));

        if (!item.getNote().getId().equals(noteId)) {
            throw new IllegalArgumentException("Note item does not belong to the specified note.");
        }

        noteItemRepository.delete(item);
    }

    private NoteResponse toNoteResponse(Note note) {
        UserResponse createdByResponse = UserResponse.builder()
                .id(note.getCreatedBy().getId())
                .email(note.getCreatedBy().getEmail())
                .name(note.getCreatedBy().getName())
                .avatarUrl(note.getCreatedBy().getAvatarUrl())
                .createdAt(note.getCreatedBy().getCreatedAt())
                .build();

        List<NoteItemResponse> items = noteItemRepository.findByNoteIdOrderByOrderIndex(note.getId())
                .stream()
                .map(this::toNoteItemResponse)
                .collect(Collectors.toList());

        return NoteResponse.builder()
                .id(note.getId())
                .coupleId(note.getCouple().getId())
                .createdBy(createdByResponse)
                .title(note.getTitle())
                .category(note.getCategory())
                .syncedWithPartner(note.isSyncedWithPartner())
                .items(items)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private NoteItemResponse toNoteItemResponse(NoteItem item) {
        return NoteItemResponse.builder()
                .id(item.getId())
                .noteId(item.getNote().getId())
                .content(item.getContent())
                .itemType(item.getItemType())
                .checked(item.isChecked())
                .urgent(item.isUrgent())
                .orderIndex(item.getOrderIndex())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
