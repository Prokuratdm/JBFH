package com.par.jbfh.training.entity;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.TrainingCycle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_programs")
@Getter
@Setter
@NoArgsConstructor
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_level", nullable = false, length = 50)
    private LoadLevel loadLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrainingCycle cycle;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}