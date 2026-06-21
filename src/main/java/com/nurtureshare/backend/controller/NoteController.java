package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.dto.request.CreateNoteItemRequest;
import com.nurtureshare.backend.dto.request.CreateNoteRequest;
import com.nurtureshare.backend.dto.request.UpdateNoteItemRequest;
import com.nurtureshare.backend.dto.response.ApiResponse;
import com.nurtureshare.backend.dto.response.NoteItemResponse;
import com.nurtureshare.backend.dto.response.NoteResponse;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController extends BaseController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotes(
            @RequestParam(required = false) String category) {
        User currentUser = getCurrentUser();
        List<NoteResponse> notes = noteService.getNotes(currentUser, category);
        return ResponseEntity.ok(ApiResponse.ok(notes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @Valid @RequestBody CreateNoteRequest request) {
        User currentUser = getCurrentUser();
        NoteResponse note = noteService.createNote(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Note created successfully", note));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        noteService.deleteNote(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<NoteItemResponse>> addNoteItem(
            @PathVariable UUID id,
            @Valid @RequestBody CreateNoteItemRequest request) {
        User currentUser = getCurrentUser();
        NoteItemResponse item = noteService.addNoteItem(currentUser, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Note item added successfully", item));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<NoteItemResponse>> updateNoteItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateNoteItemRequest request) {
        User currentUser = getCurrentUser();
        NoteItemResponse item = noteService.updateNoteItem(currentUser, id, itemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Note item updated successfully", item));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteNoteItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        User currentUser = getCurrentUser();
        noteService.deleteNoteItem(currentUser, id, itemId);
        return ResponseEntity.noContent().build();
    }
}
