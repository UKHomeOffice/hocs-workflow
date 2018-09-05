package uk.gov.digital.ho.hocs.workflow.infoClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.model.*;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class InfoClient {

    private final String INFO_SERVICE;
    private final RestTemplate restTemplate;
    private final String CASE_SERVICE_AUTH;

    @Autowired
    public InfoClient(RestTemplate restTemp, @Value("${hocs.info-service}") String infoService, @Value("${hocs.case-service.auth}") String caseworkBasicAuth) {
        INFO_SERVICE = infoService;
        restTemplate = restTemp;
        CASE_SERVICE_AUTH = caseworkBasicAuth;
    }

   public Map<StageType, LocalDate> getDeadlines(CaseType caseType, LocalDate localDate) {

       ResponseEntity<InfoGetDeadlinesResponse> response = getWithAuth(String.format("/casetype/%s/deadlines/%s", caseType, localDate), null, InfoGetDeadlinesResponse.class);
       Map<StageType, LocalDate> deadlines = response.getBody().getDeadlines();
       return deadlines;
   }

   public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
       ResponseEntity<InfoGetNominatedPeopleResponse> response = getWithAuth(String.format("/nominatedpeople/%s", teamUUID), null, InfoGetNominatedPeopleResponse.class);
       Set<InfoNominatedPeople> nominatedPeople = response.getBody().getNominatedPeople();
        return nominatedPeople;
   }

    public Correspondent getMemberAsCorrespondent(String memberId) {

    }

    private <T,R> ResponseEntity<R> postWithAuth(String url, T request, Class<R> responseType) {
        return restTemplate.postForEntity(String.format("%s%s", INFO_SERVICE, url), new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    private <T,R> ResponseEntity<R> getWithAuth(String url, T request, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", INFO_SERVICE, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, basicAuth());
        return headers;
    }

    private String basicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(CASE_SERVICE_AUTH.getBytes(Charset.forName("UTF-8")))); }
}
