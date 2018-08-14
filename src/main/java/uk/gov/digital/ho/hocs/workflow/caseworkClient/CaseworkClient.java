package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.DocumentType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class CaseworkClient {

    private final String CASE_SERVICE;
    private final RestTemplate restTemplate;
    private final String CASE_SERVICE_AUTH;

    @Autowired
    public CaseworkClient(RestTemplate restTemp, @Value("${hocs.case-service}") String caseService, @Value("${hocs.case-service.auth}") String caseworkBasicAuth) {
        CASE_SERVICE = caseService;
        restTemplate = restTemp;
        CASE_SERVICE_AUTH = caseworkBasicAuth;
    }

    public CwCreateCaseResponse createCase(CaseType caseType) {
        log.info("Creating a case: {}", caseType);
        // TODO: Wire in.
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity<CwCreateCaseResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case",  new HttpEntity<>(request, createAuthHeaders()), CwCreateCaseResponse.class);
        if(response.getStatusCodeValue() == 200) {
            log.debug("Successfully created case: {}", caseType);
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create case; response: %s ", response.getStatusCodeValue());
        }
    }

    public CwCreateStageResponse createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        log.info("Creating a stage for Case: '{}'  Type: '{}'", caseUUID, stageType);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, userUUID, new HashMap<>());
        ResponseEntity<CwCreateStageResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/stage" ,  new HttpEntity<>(request, createAuthHeaders()), CwCreateStageResponse.class);
        if(response.getStatusCodeValue() == 200) {
            log.debug("Successfully created stage Case: '{}' Stage: '{}' Type: '{}'", caseUUID, response.getBody().getUuid(), stageType);
            log.debug("************ Dev Shortcut /case/{}/stage/{}", caseUUID, response.getBody().getUuid());
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create stage; response: %s", response.getStatusCodeValue());
        }
    }

    public CwGetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        log.info("Getting a Stage '{}' for Case: '{}'", stageUUID, caseUUID);
        ResponseEntity<CwGetStageResponse> response = restTemplate.exchange(CASE_SERVICE + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), CwGetStageResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Successfully retrieved  Stage: '{}' Case: '{}'", stageUUID, caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not create stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String,String> data) {
        CwUpdateStageRequest request = new CwUpdateStageRequest(data);
        ResponseEntity<Void> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/stage/" + stageUUID, new HttpEntity<>(request, createAuthHeaders()), Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Updated Stage: '{}'", stageUUID);
        } else {
            throw new EntityCreationException("Could not create screen; response: %s", response.getStatusCodeValue());
        }
    }

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.info("Updating Stage: '{}' for Case: '{}'", stageUUID, caseUUID);
        ResponseEntity<Void> response = getWithAuth(String.format("/stage/%s/complete", stageUUID), null, Void.class);
        if(response.getStatusCodeValue() == 200) {
            log.debug("Successfully updated Stage: '{}' for Case: '{}'",stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not update stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        log.info("Allocating Stage: '{}' for Case: '{}'", stageUUID, caseUUID);
            AllocateStageRequest request = new AllocateStageRequest(teamUUID, userUUID);
            ResponseEntity<Void> response = postWithAuth(String.format("/case/%s/stage/%s/allocate", caseUUID, stageUUID), request, Void.class);

            if(response.getStatusCodeValue() == 200) {
                log.debug("Allocated Stage: '{}' for Case: '{}'",stageUUID, caseUUID);
            } else {
                throw new EntityCreationException("Could not update stage; response: %s", response.getStatusCodeValue());
            }
    }

    public CwCreateDocumentResponse addDocument(UUID caseUUID, String name, DocumentType type){
        log.info("Creating document for Case: '{}'", caseUUID);
        CreateDocumentRequest request = new CreateDocumentRequest(name, type);
        ResponseEntity<CwCreateDocumentResponse> response = postWithAuth(String.format("/case/%s/document", caseUUID), request, CwCreateDocumentResponse.class);
        if(response.getStatusCodeValue() == 200) {
            log.debug("Successfully added Document: '{}' to Case: '{}'", response.getBody().getUuid(), caseUUID);
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create document; response: %s", response.getStatusCodeValue());
        }

    }

    private <T,R> ResponseEntity<R> postWithAuth(String url, T request, Class<R> responseType) {
        String testURL = String.format("%s%s", CASE_SERVICE, url);
        return restTemplate.postForEntity(String.format("%s%s", CASE_SERVICE, url), new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    private <T,R> ResponseEntity<R> getWithAuth(String url, T request, Class<R> responseType) {
        String testURL = String.format("%s%s", CASE_SERVICE, url);
        return restTemplate.exchange(String.format("%s%s", CASE_SERVICE, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, caseworkBasicAuth());
        return headers;
    }

    private String caseworkBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(CASE_SERVICE_AUTH.getBytes(Charset.forName("UTF-8")))); }
}
