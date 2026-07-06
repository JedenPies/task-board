package net.patrykdobrowolski.task_board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class TaskBoardApplication {

	static void main() {
		SpringApplication.run(TaskBoardApplication.class);
	}

}
