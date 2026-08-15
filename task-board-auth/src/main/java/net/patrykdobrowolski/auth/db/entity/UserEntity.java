package net.patrykdobrowolski.auth.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor @NoArgsConstructor
@Getter @Builder
@SQLRestriction("deleted = false")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;

    private String displayName;

    @Getter
    private String passwordEncoded;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private String providerId;

    private boolean deleted;
}
