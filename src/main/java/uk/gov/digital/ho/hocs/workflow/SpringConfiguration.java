package uk.gov.digital.ho.hocs.workflow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class SpringConfiguration implements WebMvcConfigurer {

    @Bean
    public RequestData createRequestData() {
        return new RequestData();
    }

    @Bean
    public RestTemplate createRestTemplate() { return new RestTemplate();}

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createRequestData());
    }
}
