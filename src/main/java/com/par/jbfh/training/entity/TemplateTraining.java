package com.par.jbfh.training.entity;

import com.par.jbfh.auth.entity.Club;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "template_trainings")
@Getter
@Setter
@NoArgsConstructor
public class TemplateTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "picture_path", length = 500)
    private String picturePath;

    @Column(name = "task_1", length = 1000)
    private String task1;

    @Column(name = "task_2", length = 1000)
    private String task2;

    @Column(name = "task_3", length = 1000)
    private String task3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

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