package net.patrykdobrowolski.task_board.config;

import lombok.Getter;
import lombok.Setter;
import net.patrykdobrowolski.task_board.domain.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class UserContextImpl implements UserContext {

    @Getter @Setter
    private String userName;
}
