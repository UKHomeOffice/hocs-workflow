package uk.gov.digital.ho.hocs.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.migration.utils.RequestResponseLoggingInterceptor;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class MigrationApplication {

	private static final Logger log = LoggerFactory.getLogger(MigrationApplication.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(MigrationApplication.class)
				.web(WebApplicationType.NONE)
				.run(args)
				.close();
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate
				= new RestTemplate(
				new BufferingClientHttpRequestFactory(
						new SimpleClientHttpRequestFactory()
				)
		);

		List<ClientHttpRequestInterceptor> interceptors
				= restTemplate.getInterceptors();
		if (CollectionUtils.isEmpty(interceptors)) {
			interceptors = new ArrayList<>();
		}
		interceptors.add(new RequestResponseLoggingInterceptor());
		restTemplate.setInterceptors(interceptors);
		return restTemplate;
	}

	@Bean
	public CommandLineRunner run(MigrationService migration)
			throws Exception {
		return args -> {
			log.info(migration.getDescription());

			if (args.length > 0){
				String mode = args[0].toUpperCase();
				List<String> fixableCaseUuids = migration.getFixableCases();

				switch (mode){
					case "LIST":
						log.info("Fixable cases found: {}", fixableCaseUuids.toString());
						break;

					case "ALL":
						for (String caseUuid: fixableCaseUuids) {
							migration.fixCase(caseUuid);
						};
						break;

					case "BATCH":
						if (args.length > 1 && !args[1].isEmpty()) {
							int fixesRequested = Integer.parseInt(args[1]);
							if (fixesRequested > 0){
								int fixesToApply = Math.min(fixesRequested, fixableCaseUuids.size());
								for (int i = 0; i < fixesToApply; i++) {
									migration.fixCase(fixableCaseUuids.get(i));
								}
							} else {
								log.error("Supplied quantity of cases to fix is invalid.");
							}
						} else {
							log.error("An argument with a quantity of cases to fix is required when using BATCH mode.");
						}
						break;

					case "SINGLE":
						if (args.length > 1 && !args[1].isEmpty()){
							if (fixableCaseUuids.contains(args[1])){
								migration.fixCase(args[1]);
							} else {
								log.error("Case UUID {} is able to be fixed in current setup.", args[1]);
							}
						} else {
							log.error("A case UUID is required for processing a single case.");
						}
						break;

					default:
						log.error("Process arg (list, single or all) must be supplied.");
				}
			} else {
				log.error("No command line arguments supplied");
			}
		};
	}
}
