package net.patrykdobrowolski.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter @Builder
public class User {

    private UUID id;
    private String login;
    private String passwordEncoded;
}
