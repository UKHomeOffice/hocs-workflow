package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;
import uk.gov.digital.ho.hocs.workflow.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;

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

    public CreateCaseworkCaseResponse createCase(UUID caseUUID, CaseDataType caseDataType, Map<String, String> data) {
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseUUID, caseDataType, data);
        ResponseEntity<CreateCaseworkCaseResponse> response = restHelper.post(serviceBaseURL, "/case", request, CreateCaseworkCaseResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Case {}, {}", response.getBody().getUuid(), response.getBody().getReference());
            return response.getBody();
        } else {
            throw new EntityCreationException("Could not create Case; response: %s ", response.getStatusCodeValue());
        }
    }

    public void updateCase(UUID caseUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(caseUUID, data);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Set Input Data for Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public GetCaseworkCaseDataResponse getCase(UUID caseUUID) {
        ResponseEntity<GetCaseworkCaseDataResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID), GetCaseworkCaseDataResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got Input for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new EntityNotFoundException("Could not get Input; response: %s", response.getStatusCodeValue());
        }
    }

    public UUID createStage(UUID caseUUID, StageType stageType, UUID teamUUID , LocalDate deadline) {
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, deadline);
        ResponseEntity<CreateCaseworkStageResponse> response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Stage: {} for Case {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Stage; response: %s", response.getStatusCodeValue());
        }
    }

    public void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID) {
        UpdateCaseworkStageUserRequest request = new UpdateCaseworkStageUserRequest(caseUUID, stageUUID, teamUUID);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Updated User for Stage {} for case {}", stageUUID, caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public void updateStageUser(UUID caseUUID, UUID stageUUID, UUID userUUID) {
        UpdateCaseworkStageUserRequest request = new UpdateCaseworkStageUserRequest(caseUUID, stageUUID, userUUID);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Updated User for Stage {} for case {}", stageUUID, caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        CompleteCaseworkStageRequest request = new CompleteCaseworkStageRequest(caseUUID,stageUUID);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Updated User for Stage {} for case {}", stageUUID, caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
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

    public void createCaseNote(UUID caseUUID, String caseNote, CaseNoteType caseNoteType) {
        log.info("caseUUID {}, case note - {}", caseUUID,caseNote);
        AddCaseworkCaseNoteDataRequest request = new AddCaseworkCaseNoteDataRequest(caseUUID, caseNote, caseNoteType);
        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Set Case Note Data for Case {}", caseUUID);
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

    public void addTopicToCase(UUID caseUUID, UUID topicUUID, String topicName) {
        AddTopicToCaseRequest request = new AddTopicToCaseRequest(caseUUID, topicUUID, topicName);

        try {
            producerTemplate.sendBody(caseQueue, objectMapper.writeValueAsString(request));
            log.info("Added Topic to Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not aa Topic: %s", e.toString());
        }
    }

}
