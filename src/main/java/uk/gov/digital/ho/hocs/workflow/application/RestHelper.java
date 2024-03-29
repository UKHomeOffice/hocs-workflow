package uk.gov.digital.ho.hocs.workflow.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Slf4j
@Component
public class RestHelper {

    private RestTemplate restTemplate;

    private RequestData requestData;

    @Autowired
    public RestHelper(RestTemplate restTemplate,
                      RequestData requestData) {
        this.restTemplate = restTemplate;
        this.requestData = requestData;
    }

    public <T, R> R post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestHelper making POST request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_POST));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST,
            new HttpEntity<>(request, createAuthHeaders()), responseType);
        return response.getBody();
    }

    public <T, R> R put(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestHelper making PUT request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_PUT));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT,
            new HttpEntity<>(request, createAuthHeaders()), responseType);
        return response.getBody();
    }

    public <R> R get(String serviceBaseURL, String url, Class<R> responseType) {
        log.info("RestHelper making GET request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_GET));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET,
            new HttpEntity<>(null, createAuthHeaders()), responseType);
        return response.getBody();
    }

    public <R> R get(String serviceBaseURL, String url, ParameterizedTypeReference<R> responseType) {
        log.info("RestHelper making GET request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_GET));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET,
            new HttpEntity<>(null, createAuthHeaders()), responseType);
        return response.getBody();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(RequestData.GROUP_HEADER, requestData.groups());
        headers.add(RequestData.USER_ID_HEADER, requestData.userId());
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.correlationId());
        return headers;
    }

}
