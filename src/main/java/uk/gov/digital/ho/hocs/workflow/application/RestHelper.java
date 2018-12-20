package uk.gov.digital.ho.hocs.workflow.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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

    public <T,R> ResponseEntity<R> post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    public <T,R> ResponseEntity<R> put(String serviceBaseURL, String url, T request, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.PUT, new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    public <T,R> ResponseEntity<R> get(String serviceBaseURL, String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    public <R> ResponseEntity<R> delete(String serviceBaseURL, String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.DELETE, new HttpEntity<>(null, createAuthHeaders()), responseType);
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
    }
