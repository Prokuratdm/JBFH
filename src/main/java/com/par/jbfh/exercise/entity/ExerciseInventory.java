package com.par.jbfh.exercise.entity;

import com.par.jbfh.inventory.entity.Inventory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "exercise_inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exercise_id", "inventory_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class ExerciseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    public ExerciseInventory(Exercise exercise, Inventory inventory) {
        this.exercise = exercise;
        this.inventory = inventory;
    }
}