package net.patrykdobrowolski.task_board.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
public class ErrorDto {

    private final String message;
    @Singular
    private final List<Detail> details;

    @Builder
    @Getter
    public static class Detail {

        private final String message;
    }
}
