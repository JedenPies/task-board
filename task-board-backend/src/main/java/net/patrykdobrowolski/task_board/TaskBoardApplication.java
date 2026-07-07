package net.patrykdobrowolski.task_board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories @EnableAsync
public class TaskBoardApplication {

	static void main() {
		SpringApplication.run(TaskBoardApplication.class);
	}

}
