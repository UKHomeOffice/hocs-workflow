package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CREATE_CASE_SUCCESS;

@Slf4j
@Component
public class CaseworkClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public CaseworkClient(RestHelper restHelper,
                          @Value("${hocs.case-service}") String caseService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseService;
    }

    public CreateCaseworkCaseResponse createCase(CaseDataType caseDataType, Map<String, String> data, LocalDate dateReceived, LocalDate deadline) {
        CaseType caseType = new CaseType(caseDataType.getType(), caseDataType.getShortCode(), caseDataType.getType());
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseType, data, dateReceived, deadline);
        ResponseEntity<CreateCaseworkCaseResponse> response = restHelper.post(serviceBaseURL, "/case", request, CreateCaseworkCaseResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Case {}, {}", response.getBody().getUuid(), response.getBody().getReference(), value(EVENT, CREATE_CASE_SUCCESS));
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Case; response: %s ", response.getStatusCodeValue()), CREATE_CASE_FAILURE);
        }
    }

    public void updateCase(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/data", caseUUID, stageUUID) , request, String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondent) {
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryCorrespondent", caseUUID, stageUUID) , primaryCorrespondent, String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
    }

    public void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopic) {
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryTopic", caseUUID, stageUUID) , primaryTopic, String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
    }

    public GetCaseworkCaseDataResponse getCase(UUID caseUUID) {
        ResponseEntity<GetCaseworkCaseDataResponse> response = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID), GetCaseworkCaseDataResponse.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Case; response: %s", response.getStatusCodeValue()), CASE_NOT_FOUND);
        }
    }

    public String getCaseType(UUID caseUUID) {
        ResponseEntity<String> response = restHelper.get(serviceBaseURL, String.format("/case/%s/type", caseUUID), String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got Type for Case: {}", caseUUID);
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Case Type; response: %s", response.getStatusCodeValue()), CASE_TYPE_NOT_FOUND);
        }
    }

    public UUID createCaseNote(UUID caseUUID, String type, String text) {
        CreateCaseNoteRequest request = new CreateCaseNoteRequest(type,text);
        ResponseEntity<UUID> response = restHelper.post(serviceBaseURL, String.format("/case/%s/note", caseUUID), request, UUID.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Note: {} for Case {}", response.getBody(), caseUUID);
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Note; response: %s", response.getStatusCodeValue()), CASE_NOTE_CREATION_FAILURE);
        }
    }

    public UUID createStage(UUID caseUUID, StageType stageType, UUID teamUUID , LocalDate deadline, String allocationType) {
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, deadline, allocationType);
        ResponseEntity<CreateCaseworkStageResponse> response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Stage: {} for Case {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Stage; response: %s", response.getStatusCodeValue()), STAGE_CREATION_FAILURE);
        }
    }

    public void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        UpdateCaseworkStageTeamRequest request = new UpdateCaseworkStageTeamRequest(caseUUID, stageUUID, teamUUID, allocationType);
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), request, String.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Updated Team {} on Stage: {} for Case {}", teamUUID, stageUUID, caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Stage; response: %s", response.getStatusCodeValue()), STAGE_TEAM_UPDATE_FAILURE);
        }
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        ResponseEntity<UUID> response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), UUID.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got User for Stage: {} for Case: {}", stageUUID, caseUUID);
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Stage User; response: %s", response.getStatusCodeValue()), STAGE_USER_NOT_FOUND);
        }
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        ResponseEntity<UUID> response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), UUID.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Got Team Stage: {} for Case: {}", stageUUID, caseUUID);
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Stage Team; response: %s", response.getStatusCodeValue()),STAGE_NOT_FOUND);
        }
    }
}
