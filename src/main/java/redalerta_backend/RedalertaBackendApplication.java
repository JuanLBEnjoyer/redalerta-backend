package redalerta_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RedalertaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedalertaBackendApplication.class, args);
	}

}
