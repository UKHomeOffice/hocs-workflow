package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BpmnService {

    private final CaseworkClient caseworkClient;
    private final CamundaClient camundaClient;
    private final InfoClient infoClient;

    @Autowired
    public BpmnService(CaseworkClient caseworkClient,
                       CamundaClient camundaClient,
                       InfoClient infoClient) {
        this.caseworkClient = caseworkClient;
        this.camundaClient = camundaClient;
        this.infoClient = infoClient;
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String allocationType, String allocationTeamString) {
        return createStage(caseUUIDString, stageUUIDString, stageTypeString, allocationType, allocationTeamString, null);
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String allocationType, String allocationTeamString, String allocatedUserId) {
        log.debug("Creating or Updating Stage {} for case {}", stageTypeString, caseUUIDString);

        UUID teamUUID = deriveTeamUUID(caseUUIDString, stageTypeString, allocationTeamString);
        UUID userUUID = deriveUserUUID(caseUUIDString, stageTypeString, allocatedUserId);

        String resultStageUUID;
        if (StringUtils.hasText(stageUUIDString)) {
            log.debug("Stage {} already exists for case {}, recreating stage {}", stageTypeString, caseUUIDString, stageUUIDString);
            recreateStage(caseUUIDString, stageUUIDString, stageTypeString, allocationType, teamUUID, userUUID);
            resultStageUUID = stageUUIDString;
            log.info("Updated Stage {} for Case {}", stageUUIDString, caseUUIDString);
        } else {
            log.debug("Creating new stage {} for case {}, assigning to team {}", stageTypeString, caseUUIDString, teamUUID);
            resultStageUUID = caseworkClient.createStage(UUID.fromString(caseUUIDString), stageTypeString, teamUUID, userUUID, allocationType).toString();
            log.info("Created Stage {} for Case {}", resultStageUUID, caseUUIDString);
        }

        return resultStageUUID;
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        caseworkClient.updateStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), null, null);
        log.info("Completed Stage {} for Case {}", stageUUIDString, caseUUIDString);
    }

    public void completeCase(String caseUUIDString) {
        caseworkClient.completeCase(UUID.fromString(caseUUIDString), true);
        log.info("Completed Case {}", caseUUIDString);
    }

    public void calculateTotals(String caseUUIDString, String stageUUIDString, String listName){
        caseworkClient.calculateTotals(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), listName);
        log.info("Calculated totals WCS for Case {}", caseUUIDString);
    }

    public void updateDeadline(String caseUUIDString, String stageUUIDString, String dateReceived) {
        caseworkClient.updateDateReceived(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), LocalDate.parse(dateReceived));
    }

    public void updatePrimaryCorrespondent(String caseUUIDString, String stageUUIDString, String correspondentUUIDString) {
        caseworkClient.updatePrimaryCorrespondent(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), UUID.fromString(correspondentUUIDString));
        log.info("Updated Primary Correspondent for Case {}", caseUUIDString);
    }

    public void updatePrimaryTopic(String caseUUIDString, String stageUUIDString, String topicUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID topicUUID = UUID.fromString(topicUUIDString);

        caseworkClient.updatePrimaryTopic(caseUUID, stageUUID, topicUUID);
    }

    public void updateTeamsForPrimaryTopic(String caseUUIDString, String stageUUIDString, String topicUUIDString, String stageType, String teamNameKey, String teamUUIDKey) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID topicUUID = UUID.fromString(topicUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();
        TeamDto teamDto = infoClient.getTeamForTopicAndStage(caseUUID, topicUUID, stageType);
        teamsForTopic.put(teamNameKey, teamDto.getUuid().toString());
        teamsForTopic.put(teamUUIDKey, teamDto.getDisplayName());
        camundaClient.updateTask(stageUUID, teamsForTopic);
        caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);

        log.debug("######## Updated Primary Topic ########");
    }

    public void updateTeamSelection(String caseUUIDString, String stageUUIDString, String draftingUUIDString, String privateOfficeUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();

        if (StringUtils.hasText(draftingUUIDString)) {
            log.info("Overwriting draft team with {}", draftingUUIDString);
            TeamDto draftingTeam = infoClient.getTeam(UUID.fromString(draftingUUIDString));
            teamsForTopic.put("DraftingTeamUUID", draftingTeam.getUuid().toString());
            teamsForTopic.put("DraftingTeamName", draftingTeam.getDisplayName());
        }

        if (StringUtils.hasText(privateOfficeUUIDString)) {
            log.info("Overwriting po team with {}", privateOfficeUUIDString);
            TeamDto pOTeam = infoClient.getTeam(UUID.fromString(privateOfficeUUIDString));
            teamsForTopic.put("POTeamUUID", pOTeam.getUuid().toString());
            teamsForTopic.put("POTeamName", pOTeam.getDisplayName());
        }

        if (!teamsForTopic.isEmpty()) {
            camundaClient.updateTask(stageUUID, teamsForTopic);
            caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
        }

        log.debug("######## Updated Team Selection ########");
    }

    public void updatePOTeamSelection(String caseUUIDString, String stageUUIDString, String privateOfficeUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();

        if (StringUtils.hasText(privateOfficeUUIDString)) {
            log.info("Overwriting po team with {}", privateOfficeUUIDString);
            TeamDto pOTeam = infoClient.getTeam(UUID.fromString(privateOfficeUUIDString));
            teamsForTopic.put("PrivateOfficeOverridePOTeamUUID", pOTeam.getUuid().toString());
            teamsForTopic.put("PrivateOfficeOverridePOTeamName", pOTeam.getDisplayName());
        }

        if (!teamsForTopic.isEmpty()) {
            camundaClient.updateTask(stageUUID, teamsForTopic);
            caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
        }

        log.debug("######## Updated Team Selection at PO ########");
    }

    public void updateValue(String caseUUIDString, String stageUUIDString, String key, String value) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        log.info("Update {} key to {} value", key, value);
        if (StringUtils.hasText(key)){
            Map<String, String> updateValue = Map.of(key, value);
            camundaClient.updateTask(stageUUID, updateValue);
            caseworkClient.updateCase(caseUUID, stageUUID, updateValue);
        }
    }

    public void updateCaseValue(String caseUUIDString, String stageUUIDString, String key, String value) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        log.info("Update {} key to {} value", key, value);
        if (StringUtils.hasText(key)){
            Map<String, String> updateValue = Map.of(key, value);
            caseworkClient.updateCase(caseUUID, stageUUID, updateValue);
        }
    }

    public void updateAllocationNote(String caseUUIDString, String stageUUIDString, String allocationNote, String allocationNoteType) {
        log.debug("######## Save Allocation Note ########");
        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), allocationNoteType, allocationNote);
        log.info("Adding Casenote to Case: {}", caseUUIDString);
    }

    private UUID deriveTeamUUID(String caseUUIDString, String stageTypeString, String allocationTeamString) {
        UUID teamUUID;
        if (StringUtils.isEmpty(allocationTeamString)) {
            log.debug("Getting Team selection from Info Service for stage {} for case {}", stageTypeString, caseUUIDString);
            teamUUID = infoClient.getTeamForStageType(stageTypeString);
        } else {
            log.debug("Overriding Team selection with {} for stage {} for case {}", allocationTeamString, stageTypeString, caseUUIDString);
            teamUUID = UUID.fromString(allocationTeamString);
        }

        return teamUUID;

    }

    private UUID deriveUserUUID(String caseUUIDString, String stageTypeString, String allocatedUserId) {
        UUID userUUID = null;
        if (!StringUtils.isEmpty(allocatedUserId)) {
            log.debug("Assigning user {} to stage {} for case {}", allocatedUserId, stageTypeString, caseUUIDString);
            userUUID = UUID.fromString(allocatedUserId);
        }
        return userUUID;
    }

    private void recreateStage(String caseUUIDString, String stageUUIDString, String stageType, String allocationType, UUID teamUUID, UUID userUUID) {

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        caseworkClient.recreateStage(caseUUID, stageUUID, stageType);

        log.debug("Stage already exists for case {}, assigning to team {}", caseUUID, teamUUID);
        caseworkClient.updateStageTeam(caseUUID, stageUUID, teamUUID, allocationType);

        if (userUUID != null) {
            caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);
        }

    }

}