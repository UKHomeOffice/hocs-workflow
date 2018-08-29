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
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.*;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class CaseworkClient {

    private final String caseServiceUrl;
    private final String caseServiceAuth;
    private final RestTemplate restTemplate;

    @Autowired
    public CaseworkClient(RestTemplate restTemp, @Value("${hocs.case-service}") String caseService, @Value("${hocs.case-service.auth}") String caseworkBasicAuth) {
        caseServiceUrl = caseService;
        caseServiceAuth = caseworkBasicAuth;
        restTemplate = restTemp;
    }

    public CreateCaseworkCaseResponse createCase(CaseType caseType, LocalDate dateReceived) {
        log.info("Creating Case: {}", caseType);
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseType, dateReceived);
        ResponseEntity<CreateCaseworkCaseResponse> response = postWithAuth("/case", request, CreateCaseworkCaseResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Created Case: {}, {}", response.getBody().getUuid(), response.getBody().getReference());
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create Case; response: %s ", response.getStatusCodeValue());
        }
    }

    public UUID createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        log.info("Creating Stage for Case: {}  Type: {}", caseUUID, stageType);
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, userUUID, new HashMap<>());
        ResponseEntity<CreateCaseworkStageResponse> response = postWithAuth(String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Created Stage: {} Case: {}",response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String,String> data) {
        log.info("Updating Stage: {} for Case: {}", stageUUID, caseUUID);
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        ResponseEntity<Void> response = postWithAuth(String.format("/case/%s/input", caseUUID), request, Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Updated Stage: {} for Case: {}", stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not update Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.info("Completing Stage: {} for Case: {}", stageUUID, caseUUID);
        ResponseEntity<Void> response = getWithAuth(String.format("/case/%s/stage/%s/complete", caseUUID, stageUUID), Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Completed Stage: {} for Case: {}", stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not complete Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCaseworkStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage: {} for Case: {}", stageUUID, caseUUID);
        ResponseEntity<GetCaseworkStageResponse> response = getWithAuth(String.format("/case/%s/stage/%s", caseUUID, stageUUID), GetCaseworkStageResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not create Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        log.debug("Allocating Stage {}, Case {}", stageUUID, caseUUID);
        AllocateCaseworkStageRequest request = new AllocateCaseworkStageRequest(teamUUID, userUUID);
        ResponseEntity<Void> response = postWithAuth(String.format("/case/%s/stage/%s/allocate", caseUUID, stageUUID), request, Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Allocated Stage: {}, Case {}", stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not allocate Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void createDeadlines(UUID caseUUID, Set<Deadline> deadlines) {
        log.debug("Creating Deadlines, for Case {}", caseUUID);
        CreateCaseworkDeadlinesRequest request = new CreateCaseworkDeadlinesRequest(deadlines);
        ResponseEntity<Void> response = postWithAuth(String.format("/case/%s/deadline", caseUUID), request, Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Created Deadlines for Case {}", caseUUID);
        } else {
            throw new EntityCreationException("Could not create Deadlines; response: %s", response.getStatusCodeValue());
        }
    }

    public void createCorrespondent(UUID caseUUID, Correspondent correspondent) {
        log.debug("Creating Correspondent, Case {}", caseUUID);
        CreateCaseworkCorrespondentRequest request = new CreateCaseworkCorrespondentRequest(correspondent);
        ResponseEntity<Void> response = postWithAuth(String.format("/case/%s/correspondent", caseUUID), request, Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Created Correspondent, for Case {}", caseUUID);
        } else {
            throw new EntityCreationException("Could not create Correspondent; response: %s", response.getStatusCodeValue());
        }
    }

    public UUID createDocument(UUID caseUUID, String displayName, DocumentType type){
        log.debug("Creating Document, Case {}", caseUUID);
        CreateCaseworkDocumentRequest request = new CreateCaseworkDocumentRequest(displayName, type);
        ResponseEntity<CreateCaseworkDocumentResponse> response = postWithAuth(String.format("/case/%s/document", caseUUID), request, CreateCaseworkDocumentResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Created Document {}, Case {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Document; response: %s", response.getStatusCodeValue());
        }
    }

    private <T,R> ResponseEntity<R> postWithAuth(String url, T request, Class<R> responseType) {
        return restTemplate.postForEntity(String.format("%s%s", caseServiceUrl, url), new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    private <R> ResponseEntity<R> getWithAuth(String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", caseServiceUrl, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, caseworkBasicAuth());
        return headers;
    }

    private String caseworkBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(caseServiceAuth.getBytes(Charset.forName("UTF-8")))); }
}
