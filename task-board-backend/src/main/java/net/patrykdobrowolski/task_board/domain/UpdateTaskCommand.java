package net.patrykdobrowolski.task_board.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@Getter
public class UpdateTaskCommand {

    private final String newTitle;
    private final String newDescription;
}
