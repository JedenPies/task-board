package net.patrykdobrowolski.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class User {

    private UUID id;
    private String login;
    private String passwordEncoded;
}
