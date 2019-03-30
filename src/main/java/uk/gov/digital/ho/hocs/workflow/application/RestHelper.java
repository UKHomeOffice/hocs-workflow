package uk.gov.digital.ho.hocs.workflow.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.nio.charset.Charset;
import java.util.Base64;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Slf4j
@Component
public class RestHelper {

    private String basicAuth;

    private RestTemplate restTemplate;

    private RequestData requestData;

    @Autowired
    public RestHelper(RestTemplate restTemplate, @Value("${hocs.basicauth}") String basicAuth, RequestData requestData) {
        this.restTemplate = restTemplate;
        this.basicAuth = basicAuth;
        this.requestData = requestData;
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <T,R> R post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders()), responseType);
        return validateResponse(response);
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <T,R> R put(String serviceBaseURL, String url, T request, Class<R> responseType) {
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT, new HttpEntity<>(request, createAuthHeaders()), responseType);
        return validateResponse(response);
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <R> R get(String serviceBaseURL, String url, Class<R> responseType) {
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
        return validateResponse(response);
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <R> R get(String serviceBaseURL, String url, ParameterizedTypeReference<R> responseType) {
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
        return validateResponse(response);
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <R> R delete(String serviceBaseURL, String url, Class<R> responseType) {
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.DELETE, new HttpEntity<>(null, createAuthHeaders()), responseType);
        return validateResponse(response);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, getBasicAuth());
        headers.add(RequestData.GROUP_HEADER, requestData.groups());
        headers.add(RequestData.USER_ID_HEADER, requestData.userId());
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.correlationId());
        return headers;
    }

    private String getBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(Charset.forName("UTF-8")))); }

    private static <T> T validateResponse(ResponseEntity<T>  responseEntity) {
        if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            if(responseEntity.hasBody()) {
                return responseEntity.getBody();
            } else {
                log.error("Server returned malformed response {}", responseEntity.getStatusCodeValue() , value(EVENT, REST_HELPER_MALFORMED_RESPONSE));
                throw new ApplicationExceptions.ResourceServerException("Server returned malformed response", REST_HELPER_MALFORMED_RESPONSE );
            }
        } else if(responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            log.error("Server returned not found response {}", responseEntity.getStatusCodeValue() , value(EVENT, REST_HELPER_MALFORMED_RESPONSE));
            throw new ApplicationExceptions.ResourceNotFoundException("Server returned not found response", REST_HELPER_NOT_FOUND);
        } else {
            log.error("Server returned invalid response {}", responseEntity.getStatusCodeValue() , value(EVENT, REST_HELPER_MALFORMED_RESPONSE));
            throw new ApplicationExceptions.ResourceServerException("Server returned invalid response", REST_HELPER_INTERNAL_SERVER_ERROR);
        }
    }
}
