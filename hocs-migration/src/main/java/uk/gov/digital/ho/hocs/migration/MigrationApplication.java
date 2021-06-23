package uk.gov.digital.ho.hocs.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class MigrationApplication {

	private static final Logger log = LoggerFactory.getLogger(MigrationApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MigrationApplication.class, args).close();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(Migration migration)
			throws Exception {
		return args -> {
			migration.fixCase("3a7f36f1-3ae5-416f-b709-03a1c2976dc1");
		};
	}
}
