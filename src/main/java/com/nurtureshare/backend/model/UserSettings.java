package com.nurtureshare.backend.model;

import com.nurtureshare.backend.model.enums.AppMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private boolean notificationsEnabled = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppMode appMode = AppMode.PREGNANCY;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
