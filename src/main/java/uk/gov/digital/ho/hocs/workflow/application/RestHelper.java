package uk.gov.digital.ho.hocs.workflow.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class RestHelper {

    private String basicAuth;

    private RestTemplate restTemplate;

    @Autowired
    public RestHelper(RestTemplate restTemplate, @Value("${hocs.basicauth}") String basicAuth) {
        this.restTemplate = restTemplate;
        this.basicAuth = basicAuth;
    }

    public <T,R> ResponseEntity<R> post(String serviceBaseURL, String url, T request, Class<R> responseType) {

        try {
            String response = restTemplate.exchange("https://clamav.{{.KUBE_NAMESPACE}}.svc.cluster.local", HttpMethod.GET, null, String.class).getBody();
            log.info(response);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders()), responseType);

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
        return headers;
    }

    private String getBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(Charset.forName("UTF-8")))); }

}
