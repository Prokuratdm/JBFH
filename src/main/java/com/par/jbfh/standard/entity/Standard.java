package com.par.jbfh.standard.entity;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.enums.StandardUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "standards")
@Getter
@Setter
@NoArgsConstructor
public class Standard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExerciseType type;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StandardUnit unit;

    @Column(name = "control_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal controlValue;

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