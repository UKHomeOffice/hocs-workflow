package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
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

    private final String caseQueue;

    private final ProducerTemplate producerTemplate;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public CaseworkClient(RestTemplate restTemp,
                          ProducerTemplate producerTemplate,
                          ObjectMapper objectMapper,
                          @Value("${hocs.case-service}") String caseService,
                          @Value("${hocs.case-service.auth}") String caseworkBasicAuth,
                          @Value("${case.queue}") String caseQueue) {
        this.producerTemplate = producerTemplate;
        this.caseServiceUrl = caseService;
        this.objectMapper = objectMapper;
        this.caseServiceAuth = caseworkBasicAuth;
        this.restTemplate = restTemp;
        this.caseQueue = caseQueue;
    }

    public CreateCaseworkCaseResponse createCase(CaseType caseType) {
        log.info("Creating Case: {}", caseType);
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseType);
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

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.info("Completing Stage: {} for Case: {}", stageUUID, caseUUID);
        ResponseEntity<Void> response = getWithAuth(String.format("/case/%s/stage/%s/close", caseUUID, stageUUID), Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.debug("Completed Stage: {} for Case: {}", stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not complete Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void setInputData(UUID caseUUID, Map<String,String> data) {
        log.debug("Setting Input Data, Case {}", caseUUID);
        UpdateCaseworkInputDataRequest request = new UpdateCaseworkInputDataRequest(caseUUID, data);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Set Input Data, Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public void removeInputData(UUID caseUUID, Set<String> dataToRemove) {
        Map<String, String> tempData = new HashMap<>();
        dataToRemove.forEach(d -> tempData.put(d, ""));

        this.setInputData(caseUUID, tempData);
    }

    public void createDeadlines(UUID caseUUID, Map<StageType, LocalDate> deadlines) {
        log.debug("Creating Deadlines, for Case {}", caseUUID);
        UpdateCaseworkDeadlinesRequest request = new UpdateCaseworkDeadlinesRequest(caseUUID, deadlines);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Deadlines for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public void createCorrespondent(UUID caseUUID, Correspondent correspondent) {
        log.debug("Creating Correspondent, Case {}", caseUUID);
        CreateCaseworkCorrespondentRequest request = new CreateCaseworkCorrespondentRequest(caseUUID, correspondent);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Correspondent, for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not create Correspondent: %s", e.toString());
        }
    }

    public void createReference(UUID caseUUID, ReferenceType referenceType, String reference) {
        log.debug("Creating Correspondent, Case {}", caseUUID);
        CreateCaseworkReferenceRequest request = new CreateCaseworkReferenceRequest(caseUUID, reference, referenceType);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Correspondent, Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not create Reference: %s", e.toString());
        }
    }

    public GetCaseworkStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage: {} for Case: {}", stageUUID, caseUUID);
        ResponseEntity<GetCaseworkStageResponse> response = getWithAuth(String.format("/case/%s/stage/%s", caseUUID, stageUUID), GetCaseworkStageResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCaseworkInputResponse getInput(UUID caseUUID) {
        log.debug("Getting Input for Case: {}", caseUUID);
        ResponseEntity<GetCaseworkInputResponse> response = getWithAuth(String.format("/case/%s/input", caseUUID), GetCaseworkInputResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Got Input for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get Input; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCaseworkCaseTypeResponse getCaseTypeForCase(UUID caseUUID){
        log.debug("Getting caseType for Case: {}", caseUUID);
        ResponseEntity<GetCaseworkCaseTypeResponse> response = getWithAuth(String.format("/case/%s/casetype", caseUUID), GetCaseworkCaseTypeResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Got caseType for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get caseType; response: %s", response.getStatusCodeValue());
        }
    }

    public void addTopicToCase(UUID caseUUID, UUID topicUUID) {
        log.debug("adding topic to Case: {}", caseUUID);
        AddTopicToCaseRequest request = new AddTopicToCaseRequest(topicUUID);
        ResponseEntity<Void> response = postWithAuth(String.format("/case/%s/topic", caseUUID), request, Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Added topic {}, to Case {}", topicUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not add topic; response: %s", response.getStatusCodeValue());
        }
    }

    public void deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        log.debug("deleting topic from Case: {}", caseUUID);
        ResponseEntity<Void> response = deleteWithAuth(String.format("/case/%s/topic/%s", caseUUID, topicUUID), Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("deleted topic {}, from Case {}", topicUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not add topic; response: %s", response.getStatusCodeValue());
        }
    }

    private <T,R> ResponseEntity<R> postWithAuth(String url, T request, Class<R> responseType) {
        return restTemplate.postForEntity(String.format("%s%s", caseServiceUrl, url), new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    private <R> ResponseEntity<R> getWithAuth(String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", caseServiceUrl, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private <R> ResponseEntity<R> deleteWithAuth(String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", caseServiceUrl, url), HttpMethod.DELETE, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, caseworkBasicAuth());
        return headers;
    }

    private String caseworkBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(caseServiceAuth.getBytes(Charset.forName("UTF-8")))); }

}
