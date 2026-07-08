package net.patrykdobrowolski.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class UserNotFoundException extends Exception {

    private final String login;
}
