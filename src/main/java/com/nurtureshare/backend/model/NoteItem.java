package com.nurtureshare.backend.model;

import com.nurtureshare.backend.model.enums.NoteItemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "note_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NoteItemType itemType = NoteItemType.CHECKLIST;

    @Builder.Default
    private boolean checked = false;

    @Builder.Default
    private boolean urgent = false;

    private int orderIndex;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
