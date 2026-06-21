package com.nurtureshare.backend.model;

import com.nurtureshare.backend.model.enums.AppMode;
import com.nurtureshare.backend.model.enums.BabyGender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "pregnancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregnancy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppMode appMode = AppMode.PREGNANCY;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BabyGender babyGender = BabyGender.UNKNOWN;

    private String babyName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Transient
    public int getCurrentWeek() {
        if (dueDate == null) return 1;
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        int weeksLeft = (int) Math.max(0, daysLeft / 7);
        return Math.min(40, Math.max(1, 40 - weeksLeft));
    }

    @Transient
    public int getBabyAgeWeeks() {
        if (dueDate == null) return 0;
        long daysSince = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return (int) Math.max(0, daysSince / 7);
    }

    @Transient
    public String getTrimester() {
        int week = getCurrentWeek();
        if (week <= 13) return "1st Trimester";
        if (week <= 26) return "2nd Trimester";
        return "3rd Trimester";
    }

    @Transient
    public String getBabyStage() {
        int weeks = getBabyAgeWeeks();
        if (weeks <= 4)  return "Newborn";
        if (weeks <= 12) return "Young Infant";
        if (weeks <= 26) return "Infant";
        if (weeks <= 52) return "Older Infant";
        return "Toddler";
    }

    @Transient
    public int getPercentComplete() {
        return (int) Math.min(100, (getCurrentWeek() / 40.0) * 100);
    }

    @Transient
    public int getWeeksLeft() {
        return Math.max(0, 40 - getCurrentWeek());
    }
}
