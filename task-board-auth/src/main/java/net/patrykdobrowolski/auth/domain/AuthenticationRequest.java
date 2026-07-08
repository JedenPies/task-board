package net.patrykdobrowolski.auth.domain;

import lombok.Builder;

@Builder
public record AuthenticationRequest(String login, String password) {

}
