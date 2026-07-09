package net.patrykdobrowolski.auth.domain;

import lombok.Builder;

@Builder
public record AuthenticateWithPasswordCommand(String login, String password) {

}
