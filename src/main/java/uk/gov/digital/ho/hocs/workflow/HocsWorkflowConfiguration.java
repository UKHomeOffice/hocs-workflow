package uk.gov.digital.ho.hocs.workflow;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CaseworkClient;

@Configuration

public class HocsWorkflowConfiguration implements WebMvcConfigurer {

    @Bean
    public RequestData createRequestData() {
        return new RequestData();
    }

    @Bean
    public RestTemplate createRestTemplate() { return new RestTemplate();}

    @Bean
    public CaseworkClient createCaseworkClient(RestTemplate restTemplate,  @Value("${hocs.case-service}") String caseService) { return new CaseworkClient(restTemplate, caseService);}

    @Bean
    public CamundaClient createCamundaClient(RuntimeService runtimeService, TaskService taskService) { return new CamundaClient(runtimeService, taskService); }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createRequestData());
    }
}
