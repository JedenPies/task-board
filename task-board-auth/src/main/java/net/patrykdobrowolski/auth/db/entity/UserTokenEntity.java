package net.patrykdobrowolski.auth.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_tokens")
@AllArgsConstructor @NoArgsConstructor
@Getter @Builder
public class UserTokenEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String refreshToken;
    private Instant validUntil;

    private Instant usedAt;
    @Builder.Default
    private Boolean isRevoked = false;
    private String replacedBy;

    @PrePersist
    protected void generateId() {
        this.id = UUID.randomUUID();
    }
}
