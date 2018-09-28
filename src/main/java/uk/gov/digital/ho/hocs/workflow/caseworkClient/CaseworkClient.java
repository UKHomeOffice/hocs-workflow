package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.*;
import uk.gov.digital.ho.hocs.workflow.dto.GetCaseTopicsResponse;
import uk.gov.digital.ho.hocs.workflow.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.Correspondent;
import uk.gov.digital.ho.hocs.workflow.model.ReferenceType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class CaseworkClient {

    private final String caseQueue;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public CaseworkClient(RestHelper restHelper,
                          @Value("${hocs.case-service}") String caseService,
                          ProducerTemplate producerTemplate,
                          @Value("${case.queue}") String caseQueue,
                          ObjectMapper objectMapper) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseService;

        this.producerTemplate = producerTemplate;
        this.caseQueue = caseQueue;
        this.objectMapper = objectMapper;
    }

    public CreateCaseworkCaseResponse createCase(CaseType caseType) {
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseType);
        ResponseEntity<CreateCaseworkCaseResponse> response = restHelper.post(serviceBaseURL, "/case", request, CreateCaseworkCaseResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Case {}, {}", response.getBody().getUuid(), response.getBody().getReference());
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create Case; response: %s ", response.getStatusCodeValue());
        }
    }

    public UUID createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, userUUID, new HashMap<>());
        ResponseEntity<CreateCaseworkStageResponse> response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Stage: {} for Case {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        AllocateCaseworkStageRequest request = new AllocateCaseworkStageRequest(teamUUID, userUUID);
        ResponseEntity<Void> response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage/%s/allocate", caseUUID, stageUUID), request, Void.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Allocated Stage: {} for Case {}", stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not allocate Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        ResponseEntity<Void> response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/close", caseUUID, stageUUID), Void.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Completed Stage: {} for Case {}", stageUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not complete Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void setInputData(UUID caseUUID, Map<String, String> data) {
        UpdateCaseworkInputDataRequest request = new UpdateCaseworkInputDataRequest(caseUUID, data);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Set Input Data for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public void createDeadlines(UUID caseUUID, Map<StageType, LocalDate> deadlines) {
        UpdateCaseworkDeadlinesRequest request = new UpdateCaseworkDeadlinesRequest(caseUUID, deadlines);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Deadlines for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not create Deadlines: %s", e.toString());
        }
    }

    public void createCorrespondent(UUID caseUUID, Correspondent correspondent) {
        CreateCaseworkCorrespondentRequest request = new CreateCaseworkCorrespondentRequest(caseUUID, correspondent);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Correspondent for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not create Correspondent: %s", e.toString());
        }
    }

    public void createReference(UUID caseUUID, ReferenceType referenceType, String reference) {
        CreateCaseworkReferenceRequest request = new CreateCaseworkReferenceRequest(caseUUID, reference, referenceType);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Reference for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not create Reference: %s", e.toString());
        }
    }

    public GetCaseworkStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        ResponseEntity<GetCaseworkStageResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s", caseUUID, stageUUID), GetCaseworkStageResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCaseworkInputResponse getInput(UUID caseUUID) {
        ResponseEntity<GetCaseworkInputResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/input", caseUUID), GetCaseworkInputResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got Input for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get Input; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCaseworkCaseTypeResponse getCaseTypeForCase(UUID caseUUID) {
        ResponseEntity<GetCaseworkCaseTypeResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/casetype", caseUUID), GetCaseworkCaseTypeResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got caseType for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get caseType; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCorrespondentResponse getCorrespondentForCase(UUID caseUUID, UUID correspondentUUID) {
        ResponseEntity<GetCorrespondentResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/correspondent/%s", caseUUID, correspondentUUID), GetCorrespondentResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got correspondent for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get correspondent %s; response: %s", correspondentUUID, response.getStatusCodeValue());
        }
    }

    public void deleteCorrespondentFromCase(UUID caseUUID, UUID correspondentUUID) {
        ResponseEntity<Void> response = restHelper.delete(serviceBaseURL, String.format("/case/%s/correspondent/%s", caseUUID, correspondentUUID), Void.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Deleted correspondent {}, from Case {}", caseUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not delete correspondent; response: %s", response.getStatusCodeValue());
        }
    }

    public GetCaseTopicsResponse getCaseTopics(UUID caseUUID) {
        ResponseEntity<GetCaseTopicsResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/topic", caseUUID), GetCaseTopicsResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got topics for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get topics; response: %s", response.getStatusCodeValue());
        }
    }

    public void addTopicToCase(UUID caseUUID, UUID topicUUID, String topicName) {
        AddTopicToCaseRequest request = new AddTopicToCaseRequest(topicUUID, topicName);
        ResponseEntity<Void> response = restHelper.post(serviceBaseURL, String.format("/case/%s/topic", caseUUID), request, Void.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Added Topic {}, to Case {}", topicUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not add Topic; response: %s", response.getStatusCodeValue());
        }
    }

    public void deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        ResponseEntity<Void> response = restHelper.delete(serviceBaseURL, String.format("/case/%s/topic/%s", caseUUID, topicUUID), Void.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Deleted Topic {}, from Case {}", topicUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not delete Topic; response: %s", response.getStatusCodeValue());
        }
    }


}
