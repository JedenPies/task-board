package net.patrykdobrowolski.auth.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserCommand {

    private String displayName;
}
