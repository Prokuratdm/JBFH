package com.par.jbfh.training.entity;

import com.par.jbfh.auth.entity.User;
import com.par.jbfh.location.entity.Location;
import com.par.jbfh.team.entity.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "picture_path", length = 500)
    private String picturePath;

    @Column(length = 2000)
    private String description;

    @Column(name = "task_1", length = 1000)
    private String task1;

    @Column(name = "task_2", length = 1000)
    private String task2;

    @Column(name = "task_3", length = 1000)
    private String task3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_template_id")
    private TemplateTraining sourceTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

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