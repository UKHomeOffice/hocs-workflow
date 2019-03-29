package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CASEWORK_CLIENT_CREATE_CASE_SUCCESS;

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

    public CreateCaseworkCaseResponse createCase(CaseDataType caseDataType, Map<String, String> data, LocalDate dateReceived) {
        CaseType caseType = new CaseType(caseDataType.getType(), caseDataType.getShortCode(), caseDataType.getType());
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseType, data, dateReceived);
        try {
            CreateCaseworkCaseResponse response = restHelper.post(serviceBaseURL, "/case", request, CreateCaseworkCaseResponse.class);
            log.info("Created Case {}, {}", response.getUuid(), response.getReference(), value(EVENT, CASEWORK_CLIENT_CREATE_CASE_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Case %s", caseDataType), CASEWORK_CLIENT_CREATE_CASE_FAILURE);
        }
    }

    @CacheEvict(value = "CaseworkClientGetCase", key = "#caseUUID")
    public void updateCase(UUID caseUUID, Map<String, String> data) {
        try {
            restHelper.put(serviceBaseURL, String.format("/case/%s/data", caseUUID), data, String.class);
            log.info("Set Case Data for Case {}", caseUUID, value(EVENT, CASEWORK_CLIENT_CASE_UPDATE_SUCCESS));
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case %s", caseUUID), CASEWORK_CLIENT_CASE_UPDATE_FAILURE);
        }
    }

    @CacheEvict(value = "CaseworkClientGetCase", key = "#caseUUID")
    public void updatePrimaryCorrespondent(UUID caseUUID, UUID primaryCorrespondent) {
        try {
            restHelper.put(serviceBaseURL, String.format("/case/%s/primaryCorrespondent", caseUUID), primaryCorrespondent, String.class);
            log.info("Updating Primary Correspondent for Case {}", caseUUID,value(EVENT, CASEWORK_CLIENT_CASE_UPDATE_SUCCESS));
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Primary Correspondent for Case %s", caseUUID), CASEWORK_CLIENT_CASE_UPDATE_FAILURE);
        }
    }

    @CacheEvict(value = "CaseworkClientGetCase", key = "#caseUUID")
    public void updatePrimaryTopic(UUID caseUUID, UUID primaryTopic) {
        try {
            restHelper.put(serviceBaseURL, String.format("/case/%s/primaryTopic", caseUUID), primaryTopic, String.class);
            log.info("Updated Primary Topic Case {}", caseUUID, value(EVENT, CASEWORK_CLIENT_CASE_UPDATE_SUCCESS));
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Primary Topic for Case %s", caseUUID), CASEWORK_CLIENT_CASE_UPDATE_FAILURE);
        }
    }

    @Cacheable(value = "CaseworkClientGetCase", key = "#caseUUID")
    public GetCaseworkCaseDataResponse getCase(UUID caseUUID) {
        try {
            GetCaseworkCaseDataResponse response = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID), GetCaseworkCaseDataResponse.class);
            log.info("Got Case: {}", caseUUID, value(EVENT, CASEWORK_CLIENT_CASE_FOUND));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Case %s. %s", caseUUID, e.toString()), CASEWORK_CLIENT_CASE_NOT_FOUND);
        }
    }

    @Cacheable(value = "CaseworkClientGetCaseType")
    public String getCaseType(UUID caseUUID) {
            String response = getCase(caseUUID).getType().getType();
            log.info("Got Type for Case: {}", caseUUID, value(EVENT, CASEWORK_CLIENT_CASE_TYPE_FOUND));
            return response;
    }

    public void createCaseNote(UUID caseUUID, String type, String text) {
        CreateCaseNoteRequest request = new CreateCaseNoteRequest(type,text);
        try {
            restHelper.post(serviceBaseURL, String.format("/case/%s/note", caseUUID), request, String.class);
            log.info("Created Note for Case: {}", caseUUID, value(EVENT, CASEWORK_CLIENT_CASE_NOTE_CREATION_SUCCESS));
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Note for Case %s. %s",  caseUUID, e.toString()), CASEWORK_CLIENT_CASE_NOTE_CREATION_FAILURE);
        }
    }

    public UUID createStage(UUID caseUUID, StageType stageType, UUID teamUUID, String allocationType) {
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, allocationType);
        try {
            UUID response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage", caseUUID), request, UUID.class);
            log.info("Created Stage: {} for Case: {}", response, caseUUID, value(EVENT, CASEWORK_CLIENT_STAGE_CREATION_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Stage %s for Case %s. %s", stageType, caseUUID, e.toString()), CASEWORK_CLIENT_STAGE_CREATION_FAILURE);
        }
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        try {
            UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), UUID.class);
            log.info("Got User {} for Stage: {} for Case: {}", response, stageUUID, caseUUID, value(EVENT, CASEWORK_CLIENT_STAGE_USER_FOUND));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get User for Stage %s. %s", stageUUID, e.toString()), CASEWORK_CLIENT_STAGE_USER_NOT_FOUND);
        }
    }

    @CacheEvict(value = "CaseworkClientGetStageTeam", key = "#stageUUID" )
    public void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        UpdateCaseworkStageTeamRequest request = new UpdateCaseworkStageTeamRequest(teamUUID, allocationType);
        try {
            restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), request, String.class);
            log.info("Updated Team {} on Stage: {} for Case {}", teamUUID, stageUUID, caseUUID, value(EVENT, CASEWORK_CLIENT_STAGE_TEAM_UPDATE_SUCCESS));
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not update Stage %s Team to %s. %s", stageUUID, teamUUID, e.toString()), CASEWORK_CLIENT_STAGE_TEAM_UPDATE_FAILURE);
        }
    }

    @Cacheable(value = "CaseworkClientGetStageTeam", key = "#stageUUID")
    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        try {
            UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), UUID.class);
            log.info("Got Team {} Stage: {} for Case: {}", response, stageUUID, caseUUID, value(EVENT, CASEWORK_CLIENT_STAGE_FOUND));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Team for Stage %s. %s", stageUUID, e.toString()), CASEWORK_CLIENT_STAGE_NOT_FOUND);
        }
    }
}
