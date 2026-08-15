package net.patrykdobrowolski.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter @Builder
public class User {

    private UUID id;
    private String username;
    private String displayName;
    private String passwordEncoded;
    private AuthProvider authProvider;
    private String providerId;
    private boolean deleted;

    public void update(UpdateUserCommand command) {
        this.displayName = command.getDisplayName();
    }

    public void delete() {
        this.deleted = true;
        this.username = UUID.randomUUID().toString();
        this.displayName = UUID.randomUUID().toString();
        this.providerId = UUID.randomUUID().toString();
    }
}
