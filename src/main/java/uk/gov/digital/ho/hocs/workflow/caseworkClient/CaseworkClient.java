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
import uk.gov.digital.ho.hocs.workflow.dto.Topic;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.*;

import java.time.LocalDate;
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

    public CreateCaseworkCaseResponse createCase(CaseDataType caseDataType, Map<String,String> data) {
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseDataType, data);
        ResponseEntity<CreateCaseworkCaseResponse> response = restHelper.post(serviceBaseURL, "/case", request, CreateCaseworkCaseResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Case: {} ({})", response.getBody().getUuid(), response.getBody().getReference());
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create Case; response: %s ", response.getStatusCodeValue());
        }
    }

    public void updateCase(UUID caseUUID, Map<String, String> data) {
        UpdateCaseworkInputDataRequest request = new UpdateCaseworkInputDataRequest(caseUUID, data);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Updated Case: {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public GetCaseworkCaseResponse getCaseworkCase(UUID caseUUID) {
        ResponseEntity<GetCaseworkCaseResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID), GetCaseworkCaseResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Got caseType for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get case: response: %s", response.getStatusCodeValue());
        }
    }

    public UUID createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID, LocalDate deadline) {
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, userUUID, StageStatusType.CREATED, deadline);
        ResponseEntity<CreateCaseworkStageResponse> response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Stage: {} for Case: {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void updateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID, StageStatusType stageStatusType) {
        UpdateCaseworkStageOwnerRequest request = new UpdateCaseworkStageOwnerRequest(teamUUID, userUUID, stageStatusType);
        ResponseEntity<Void> response = restHelper.patch(serviceBaseURL, String.format("/case/%s/stage/%s/", caseUUID, stageUUID), request, Void.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Updated Stage: {} ({}) for Case: {}", stageUUID, stageStatusType, caseUUID);
        } else {
            throw new EntityCreationException("Could not allocate Stage; response: %s", response.getStatusCodeValue());
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

    public void createCaseNote(UUID caseUUID, CaseNoteType caseNoteType, String caseNote) {
        CreateCaseworkCaseNoteRequest request = new CreateCaseworkCaseNoteRequest(caseUUID, caseNoteType, caseNote);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Case Note Data for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Case Note Data: %s", e.toString());
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

    public GetCorrespondentResponse getCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        ResponseEntity<GetCorrespondentResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/correspondent/%s", caseUUID, correspondentUUID), GetCorrespondentResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got correspondent for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get correspondent %s; response: %s", correspondentUUID, response.getStatusCodeValue());
        }
    }

    public void deleteCorrespondent(UUID caseUUID, UUID correspondentUUID) {
        ResponseEntity<Void> response = restHelper.delete(serviceBaseURL, String.format("/case/%s/correspondent/%s", caseUUID, correspondentUUID), Void.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Deleted correspondent {}, from Case {}", caseUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not delete correspondent; response: %s", response.getStatusCodeValue());
        }
    }

    public void createTopic(UUID caseUUID, UUID topicUUID, String topicName) {
        AddTopicToCaseRequest request = new AddTopicToCaseRequest(caseUUID, topicUUID, topicName);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Created Topic for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not create Topic: %s", e.toString());
        }
    }

    public GetCaseTopicsResponse getTopics(UUID caseUUID) {
        ResponseEntity<GetCaseTopicsResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s/topic", caseUUID), GetCaseTopicsResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got topics for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get topics; response: %s", response.getStatusCodeValue());
        }
    }

    public Topic getPrimaryTopic(UUID caseUUID) {
        ResponseEntity<Topic> response = restHelper.get(serviceBaseURL, String.format("/case/%s/topic/primary", caseUUID), Topic.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got topics for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get topics; response: %s", response.getStatusCodeValue());
        }
    }

    public void deleteTopic(UUID caseUUID, UUID topicUUID) {
        ResponseEntity<Void> response = restHelper.delete(serviceBaseURL, String.format("/case/%s/topic/%s", caseUUID, topicUUID), Void.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Deleted Topic {}, from Case {}", topicUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not delete Topic; response: %s", response.getStatusCodeValue());
        }
    }
}
