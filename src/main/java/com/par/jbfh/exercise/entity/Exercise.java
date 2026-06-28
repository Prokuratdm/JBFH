package com.par.jbfh.exercise.entity;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.exercise.enums.Focus;
import com.par.jbfh.exercise.enums.PreparationType;
import com.par.jbfh.exercise.enums.TrainingPart;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "picture_path", length = 500)
    private String picturePath;

    @Column(length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExerciseType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TrainingPart trainingPart;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_focuses", joinColumns = @JoinColumn(name = "exercise_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "focus", length = 50)
    private Set<Focus> focuses = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PreparationType preparationType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}