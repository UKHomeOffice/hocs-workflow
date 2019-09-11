package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;

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

    public CreateCaseworkCaseResponse createCase(String caseDataType, Map<String, String> data, LocalDate dateReceived) {
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseDataType, data, dateReceived);
        CreateCaseworkCaseResponse response = restHelper.post(serviceBaseURL, "/case", request, CreateCaseworkCaseResponse.class);
        log.info("Created Case {}, {}", response.getUuid(), response.getReference(), value(EVENT, CREATE_CASE_SUCCESS));
        return response;
    }

    public void updateCase(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/data", caseUUID, stageUUID) , request, Void.class);
        log.info("Set Case Data for Case {}", caseUUID);
    }

    public void completeCase(UUID caseUUID, boolean completed) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/complete", caseUUID) , completed, Void.class);
        log.info("Completed Case {}", caseUUID);
    }

    public void updateDateReceived(UUID caseUUID, UUID stageUUID, LocalDate dateReceived) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/dateReceived", caseUUID, stageUUID) , dateReceived, Void.class);
        log.info("Set Date Received for Case {}", caseUUID);
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondent) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryCorrespondent", caseUUID, stageUUID) , primaryCorrespondent, Void.class);
        log.info("Set Primary Correspondent for Case {}", caseUUID);
    }

    public void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopic) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryTopic", caseUUID, stageUUID) , primaryTopic, Void.class);
        log.info("Set Primary Topic for Case {}", caseUUID);
    }

    public GetCaseworkCaseDataResponse getCase(UUID caseUUID) {
        GetCaseworkCaseDataResponse response = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID), GetCaseworkCaseDataResponse.class);
        log.info("Got Case: {}", caseUUID);
        return response;
    }

    public GetCaseworkCaseDataResponse getFullCase(UUID caseUUID) {
        GetCaseworkCaseDataResponse response = restHelper.get(serviceBaseURL, String.format("/case/%s/?full=true", caseUUID), GetCaseworkCaseDataResponse.class);
        log.info("Got Full Case: {}", caseUUID);
        return response;
    }

    public UUID createCaseNote(UUID caseUUID, String type, String text) {
        CreateCaseNoteRequest request = new CreateCaseNoteRequest(type,text);
        UUID response = restHelper.post(serviceBaseURL, String.format("/case/%s/note", caseUUID), request, UUID.class);
        log.info("Created Note: {} for Case {}", response, caseUUID);
        return response;
    }

    public UUID createStage(UUID caseUUID, String stageType, UUID teamUUID, String allocationType) {
        CreateCaseworkStageRequest request = new CreateCaseworkStageRequest(stageType, teamUUID, allocationType);
        CreateCaseworkStageResponse response = restHelper.post(serviceBaseURL, String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);
        log.info("Created Stage: {} for Case {}", response.getUuid(), caseUUID);
        return response.getUuid();
    }

    @CachePut(value = "CaseworkClientGetStageTeam", key = "{#caseUUID, #stageUUID}")
    public UUID updateStageTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        UpdateCaseworkStageTeamRequest request = new UpdateCaseworkStageTeamRequest(caseUUID, stageUUID, teamUUID, allocationType);
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), request, Void.class);
        log.info("Updated Team {} on Stage: {} for Case {}", teamUUID, stageUUID, caseUUID);
        return teamUUID;
    }

    public UUID getRegionForCase(UUID caseUUID) {
        UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/region", caseUUID), UUID.class);
        log.info("Got Region for Case: {}", caseUUID);
        return response;
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), UUID.class);
        log.info("Got User for Stage: {} for Case: {}", stageUUID, caseUUID);
        return response;
    }

    @Cacheable(value = "CaseworkClientGetStageTeam", unless = "#result == null", key = "{#caseUUID, #stageUUID}")
    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), UUID.class);
        log.info("Got Team Stage: {} for Case: {}", stageUUID, caseUUID);
        return response;
    }
}
