package net.patrykdobrowolski.auth.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor @NoArgsConstructor
@Getter @Builder
public class UserEntity {

    @Id
    private UUID id;

    private String login;
    @Getter
    private String passwordEncoded;

    @PrePersist
    protected void generateId() {
        this.id = UUID.randomUUID();
    }
}
