package net.patrykdobrowolski.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.patrykdobrowolski.auth.service.AuthProvider;

import java.util.UUID;

@AllArgsConstructor
@Getter @Builder
public class User {

    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String passwordEncoded;
    private AuthProvider authProvider;
    private String providerId;
}
