package com.par.jbfh.team.entity;

import com.par.jbfh.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "team_coaches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class TeamCoach {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public TeamCoach(Team team, User user) {
        this.team = team;
        this.user = user;
    }
}