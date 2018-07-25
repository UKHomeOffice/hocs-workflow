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

    public CwCreateCaseResponse createCase(CaseType caseType) throws EntityCreationException {
        log.info("Creating a case: {}", caseType);
        if(caseType != null) {
            CreateCaseRequest request = new CreateCaseRequest(caseType);
            ResponseEntity<CwCreateCaseResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case",  new HttpEntity<>(request, createAuthHeaders()), CwCreateCaseResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully created case: {}", caseType);
                return response.getBody();
            } else {
                throw new EntityCreationException("Could not create case; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create case; caseUUID or caseType is null!");
        }
    }

    public CwCreateStageResponse createStage(UUID caseUUID, StageType stageType) throws EntityCreationException {
        log.info("Creating a stage for case: '{}' -  type: '{}'", caseUUID, stageType);
        if(caseUUID != null && stageType != null) {
            CreateStageRequest request = new CreateStageRequest(stageType, new HashMap<>());
            ResponseEntity<CwCreateStageResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/stage" ,  new HttpEntity<>(request, createAuthHeaders()), CwCreateStageResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully created stage: '{}' - '{}' - '{}'", caseUUID, response.getBody().getUuid(), stageType);
                return response.getBody();
            } else {
                throw new EntityCreationException("Could not create stage; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create stage; caseUUID or caseType is null!");
        }
    }

    public void createScreen(UUID caseUUID, UUID stageUUID, String screenName, Map<String,String> data) throws EntityCreationException {
        log.info("Creating screen '{}' for stage: '{}'", screenName, stageUUID);
        if(caseUUID != null && stageUUID != null && screenName != null && data != null) {
            CreateScreenRequest request = new CreateScreenRequest(screenName,data);
            ResponseEntity<Void> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/stage/" + stageUUID + "/screen" ,  new HttpEntity<>(request, createAuthHeaders()), Void.class);

            if(response.getStatusCodeValue() == 200) {
                log.debug("Created screen: '{}' for case '{}'",screenName, stageUUID);
            } else {
                throw new EntityCreationException("Could not create screen; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create screen; caseUUID, stageUUID, screenName or data is null!");
        }
    }

    public void updateStage(UUID caseUUID, UUID stageUUID) throws EntityCreationException {
        log.info("Updating stage '{}' for case: '{}'", stageUUID, caseUUID);
        if(caseUUID != null && stageUUID != null) {
            ResponseEntity<Void> response = restTemplate.exchange(CASE_SERVICE + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, new HttpEntity<>(createAuthHeaders()), Void.class);

            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully updated stage: '{}' for case '{}'",stageUUID, caseUUID);
            } else {
                throw new EntityCreationException("Could not update stage; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not update stage; caseUUID or data is null!");
        }
    }

    public CwCreateDocumentResponse addDocument(UUID caseUUID, String name, DocumentType type) throws EntityCreationException {
        log.info("Creating document for case '{}'", caseUUID);
        if(caseUUID != null && name != null && type != null) {
            CreateDocumentRequest request = new CreateDocumentRequest(name, type);
            ResponseEntity<CwCreateDocumentResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/document",  new HttpEntity<>(request, createAuthHeaders()), CwCreateDocumentResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully added document ('{}') to case: '{}'", response.getBody().getUuid(), caseUUID);
                return response.getBody();
            } else {
                throw new EntityCreationException("Could not create document; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not add document; caseUUID or caseType is null!");
        }
    }

    private  HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, caseworkBasicAuth());
        return headers;
    }

    public String caseworkBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(CASE_SERVICE_AUTH.getBytes(Charset.forName("UTF-8")))); }
}
