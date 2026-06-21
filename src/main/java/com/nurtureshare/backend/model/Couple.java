package com.nurtureshare.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "couples")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user2_id")
    private User user2;

    @Column(unique = true, nullable = false, length = 10)
    private String pairingCode;

    private LocalDateTime syncedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public boolean hasUser(UUID userId) {
        return user1.getId().equals(userId) || (user2 != null && user2.getId().equals(userId));
    }

    public User getPartner(UUID currentUserId) {
        if (user1.getId().equals(currentUserId)) return user2;
        return user1;
    }

    public boolean isConnected() {
        return user2 != null;
    }
}
