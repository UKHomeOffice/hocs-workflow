package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseTagRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseworkCorrespondentRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseNoteRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkStageRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkStageResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCorrespondentsResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.MigrateCaseworkCaseRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.MigrateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.RecreateCaseworkStageRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.StageDto;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkCaseDataRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkStageTeamRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkStageUserRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkTeamStageTextRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkTeamStageTextResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateDeadlineForStagesRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateStageDeadlineRequest;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Slf4j
@Component
public class CaseworkClient {

    private final RestHelper restHelper;

    private final String serviceBaseURL;

    @Autowired
    public CaseworkClient(RestHelper restHelper, @Value("${hocs.case-service}") String caseService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseService;
    }

    public CreateCaseworkCaseResponse createCase(String caseDataType,
                                                 Map<String, String> data,
                                                 LocalDate dateReceived,
                                                 UUID fromCaseUUID) {
        CreateCaseworkCaseRequest request = new CreateCaseworkCaseRequest(caseDataType, data, dateReceived,
            fromCaseUUID);
        CreateCaseworkCaseResponse response = restHelper.post(serviceBaseURL, "/case", request,
            CreateCaseworkCaseResponse.class);
        log.info("Created Case {}, {}, {}", response.getUuid(), response.getReference(),
            value(EVENT, CREATE_CASE_SUCCESS));
        return response;
    }

    public MigrateCaseworkCaseResponse migrateCase(UUID caseUUID,
                                                   MigrateCaseworkCaseRequest migrateCaseworkCaseRequest) {
        MigrateCaseworkCaseResponse response = restHelper.post(serviceBaseURL,
            String.format("/case/%s/migrate", caseUUID), migrateCaseworkCaseRequest, MigrateCaseworkCaseResponse.class);
        log.info("Migrated Case from {} to {}, {}", caseUUID, response.getUuid(), value(EVENT, CREATE_CASE_SUCCESS));
        return response;
    }

    public void updateCase(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/data", caseUUID, stageUUID), request,
            Void.class);
        log.info("Set Case Data for Case {}", caseUUID);
    }

    public void completeCase(UUID caseUUID, boolean completed) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/complete", caseUUID), completed, Void.class);
        log.info("Completed Case {}", caseUUID);
    }

    public Map<String, String> calculateTotals(UUID caseUUID, UUID stageUUID, String listName) {
        Map<String, String> totals = restHelper.put(serviceBaseURL,
            String.format("/case/%s/stage/%s/calculateTotals", caseUUID, stageUUID), listName, Map.class);
        log.info("Calculate totals for List {} for Case {}", listName, caseUUID);
        return totals;
    }

    public void updateDateReceived(UUID caseUUID, UUID stageUUID, LocalDate dateReceived) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/dateReceived", caseUUID, stageUUID),
            dateReceived, Void.class);
        log.info("Set Date Received for Case {}", caseUUID);
    }

    public void updateDispatchDeadlineDate(UUID caseUUID, UUID stageUUID, LocalDate dispatchDate) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/dispatchDeadlineDate", caseUUID, stageUUID),
            dispatchDate, Void.class);
        log.info("Set Dispatch Deadline Date for Case {}", caseUUID);
    }

    public void updateDeadlineDays(UUID caseUUID, UUID stageUUID, int days) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/deadline", caseUUID, stageUUID), days,
            Void.class);
        log.info("Set Date Received for Case {} to {} days", caseUUID, days);
    }

    public void updateStageDeadline(UUID caseUUID, UUID stageUUID, String stageType, int days) {
        UpdateStageDeadlineRequest updateStageDeadlineRequest = new UpdateStageDeadlineRequest(stageType, days);
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/stageDeadline", caseUUID, stageUUID),
            updateStageDeadlineRequest, Void.class);
        log.info("Set Date Received for Case {} to {} days", caseUUID, days);
    }

    public void updateDeadlineForStages(UUID caseUUID, UUID stageUUID, Map<String, Integer> stageTypeAndDaysMap) {
        UpdateDeadlineForStagesRequest updateDeadlineForStagesRequest = new UpdateDeadlineForStagesRequest(
            stageTypeAndDaysMap);
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/stageDeadlines", caseUUID, stageUUID),
            updateDeadlineForStagesRequest, Void.class);
        log.info("Received {} stage deadline dates to be set for case {}", stageTypeAndDaysMap.size(), caseUUID);
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondent) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryCorrespondent", caseUUID, stageUUID),
            primaryCorrespondent, Void.class);
        log.info("Set Primary Correspondent for Case {}", caseUUID);
    }

    public void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopic) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryTopic", caseUUID, stageUUID),
            primaryTopic, Void.class);
        log.info("Set Primary Topic for Case {}", caseUUID);
    }

    public GetCaseworkCaseDataResponse getCase(UUID caseUUID) {
        GetCaseworkCaseDataResponse response = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID),
            GetCaseworkCaseDataResponse.class);
        log.info("Got Case: {}", caseUUID);
        return response;
    }

    @Deprecated(forRemoval = true)
    public GetCaseworkCaseDataResponse getFullCase(UUID caseUUID) {
        GetCaseworkCaseDataResponse response = restHelper.get(serviceBaseURL,
            String.format("/case/%s/?full=true", caseUUID), GetCaseworkCaseDataResponse.class);
        log.info("Got Full Case: {}", caseUUID);
        return response;
    }

    public UUID createCaseNote(UUID caseUUID, String type, String text) {
        CreateCaseNoteRequest request = new CreateCaseNoteRequest(type, text);
        UUID response = restHelper.post(serviceBaseURL, String.format("/case/%s/note", caseUUID), request, UUID.class);
        log.info("Created Note: {} for Case {}", response, caseUUID);
        return response;
    }

    public UUID createStage(UUID caseUUID, CreateCaseworkStageRequest request) {
        CreateCaseworkStageResponse response = restHelper.post(serviceBaseURL,
            String.format("/case/%s/stage", caseUUID), request, CreateCaseworkStageResponse.class);
        log.info("Created Stage: {} for Case {}", response.getUuid(), caseUUID);
        return response.getUuid();
    }

    @Deprecated(forRemoval = true)
    public void recreateStage(UUID caseUUID, RecreateCaseworkStageRequest recreateStageRequest) {
        restHelper.put(serviceBaseURL,
            String.format("/case/%s/stage/%s/recreate", caseUUID, recreateStageRequest.getStageUUID()),
            recreateStageRequest, Void.class);
        log.info("Recreated Stage: {} for Case {}", recreateStageRequest, caseUUID);
    }

    public UUID updateStageTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        UpdateCaseworkStageTeamRequest request = new UpdateCaseworkStageTeamRequest(caseUUID, stageUUID, teamUUID,
            allocationType);
        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), request,
            Void.class);
        log.info("Updated Team {} on Stage: {} for Case {}", teamUUID, stageUUID, caseUUID);
        return teamUUID;
    }

    public Map<String, String> updateTeamByStageAndTexts(UUID caseUUID,
                                                         UUID stageUUID,
                                                         String stageType,
                                                         String teamUUIDKey,
                                                         String teamNameKey,
                                                         String[] texts) {
        UpdateCaseworkTeamStageTextRequest request = new UpdateCaseworkTeamStageTextRequest(caseUUID, stageUUID,
            stageType, teamUUIDKey, teamNameKey, texts);
        UpdateCaseworkTeamStageTextResponse response = restHelper.put(serviceBaseURL,
            String.format("/case/%s/stage/%s/teamTexts", caseUUID, stageUUID), request,
            UpdateCaseworkTeamStageTextResponse.class);
        log.info("Updated Team {} on Stage: {} for Case {}", teamNameKey, stageUUID, caseUUID);
        return response.getTeamMap();
    }

    public void updateStageUser(UUID caseUUID, UUID stageUUID, UUID userUUID) {
        UpdateCaseworkStageUserRequest request = new UpdateCaseworkStageUserRequest(userUUID);

        restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), request,
            Void.class);
        log.info("Updated User {} for Stage: {} for Case: {}", userUUID, stageUUID, caseUUID);
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID),
            UUID.class);
        log.info("Got User for Stage: {} for Case: {}", stageUUID, caseUUID);
        return response;
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        UUID response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID),
            UUID.class);
        log.info("Got Team Stage: {} for Case: {}", stageUUID, caseUUID);
        return response;
    }

    public String getStageType(UUID caseUUID, UUID stageUUID) {
        String response = restHelper.get(serviceBaseURL, String.format("/case/%s/stage/%s/type", caseUUID, stageUUID),
            String.class);
        log.info("Got Stage Type: {} for Case: {}", stageUUID, caseUUID);
        return response;
    }

    public UUID saveCorrespondent(UUID caseUUID, UUID stageUUID, CreateCaseworkCorrespondentRequest correspondent) {
        UUID correspondentUUID = restHelper.post(serviceBaseURL,
            String.format("/case/%s/stage/%s/correspondent", caseUUID, stageUUID), correspondent, UUID.class);
        log.info("Added correspondent to Case: {}", caseUUID);
        return correspondentUUID;
    }

    @Deprecated(forRemoval = true)
    public GetAllStagesForCaseResponse getAllStagesForCase(UUID caseUUID) {
        GetAllStagesForCaseResponse response = restHelper.get(serviceBaseURL, String.format("/stage/case/%s", caseUUID),
            GetAllStagesForCaseResponse.class);
        log.info("Got all stages for case: {}", caseUUID);
        return response;
    }

    public Optional<StageDto> getActiveStage(UUID caseUUID) {
        return getAllStagesForCase(caseUUID).getStages().stream().filter(s -> s.getTeamUUID() != null).findFirst();
    }

    public GetCorrespondentsResponse getCorrespondentsForCase(UUID caseUUID) {
        log.info("Got correspondents for case: {}", caseUUID);
        GetCorrespondentsResponse correspondents = restHelper.get(serviceBaseURL,
            String.format("/case/%s/correspondent", caseUUID), GetCorrespondentsResponse.class);
        return correspondents;
    }

    public String getDataValue(String caseUUID, String variableName) {
        String response = restHelper.get(serviceBaseURL, String.format("/case/%s/data/%s", caseUUID, variableName),
            String.class);
        log.info("Got {} value: {} for Case: {}", variableName, response, caseUUID);
        return response;
    }

    public void updateDataValue(String caseUUID, String variableName, String value) {
        restHelper.put(serviceBaseURL, String.format("/case/%s/data/%s", caseUUID, variableName), value, Void.class);
        log.info("Updated {} value: {} for Case: {}", variableName, value, caseUUID);
    }

    public GetStagesResponse getActiveStage(String caseReference) {
        return restHelper.get(serviceBaseURL, String.format("/case/%s/stage", caseReference), GetStagesResponse.class);
    }

    public Date calculateDeadline(String caseType, LocalDate startDate, int workingDays) {
        LocalDate deadLineDate = restHelper.get(serviceBaseURL,
            String.format("/deadline/%s?received=%s&days=%s", caseType, startDate, workingDays), LocalDate.class);
        log.info("Calculate deadline {} ", value(EVENT, INFO_CLIENT_CALCULATE_DEADLINE));
        return Date.from(deadLineDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void mapCaseData(UUID caseUUID, Map<String, String> keyMappings) {
        restHelper.post(serviceBaseURL, String.format("/case/%s/data/map", caseUUID), keyMappings, Void.class);
        log.info("Data for caseUUID: {} mapped successfully.", caseUUID);
    }

    public void createCaseTag(UUID caseUUID, String tag) {
        CaseTagRequest caseTagRequest = new CaseTagRequest(tag);
        restHelper.post(serviceBaseURL, String.format("/case/%s/tag", caseUUID), caseTagRequest, Void.class);
    }
}
