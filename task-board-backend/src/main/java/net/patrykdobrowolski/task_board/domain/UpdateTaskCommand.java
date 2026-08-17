package net.patrykdobrowolski.task_board.domain;

import lombok.Builder;

@Builder
public record UpdateTaskCommand(String newTitle, String newDescription) {

}
