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

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString,  String allocationType, String allocationTeamString) {
        log.debug("Creating or Updating Stage {} for case {}", stageTypeString, caseUUIDString);

        UUID teamUUID;
        if(StringUtils.isEmpty(allocationTeamString)) {
            log.debug("Getting Team selection from Info Service for stage {} for case {}", stageTypeString, caseUUIDString);
            teamUUID = infoClient.getTeamForStageType(stageTypeString);
        } else {
            log.debug("Overriding Team selection with {} for stage {} for case {}", allocationTeamString, stageTypeString, caseUUIDString);
            teamUUID = UUID.fromString(allocationTeamString);
        }

        String stageUUID;
        if (stageUUIDString != null) {
            log.debug("Stage {} already exists for case {}, assigning to team {}", stageTypeString, caseUUIDString, teamUUID);
            caseworkClient.updateStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), teamUUID, allocationType);
            stageUUID = stageUUIDString;
            log.info("Updated Stage {} for Case {}", stageUUID, caseUUIDString);
        } else {
            log.debug("Creating new stage {} for case {}, assigning to team {}", stageTypeString, caseUUIDString, teamUUID);
            stageUUID = caseworkClient.createStage(UUID.fromString(caseUUIDString), stageTypeString, teamUUID, allocationType).toString();
            log.info("Created Stage {} for Case {}", stageUUID, caseUUIDString);
        }

        return stageUUID;
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        caseworkClient.updateStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), null, null);
        log.info("Completed Stage {} for Case {}", stageUUIDString, caseUUIDString);
    }

    public void completeCase(String caseUUIDString) {
        caseworkClient.completeCase(UUID.fromString(caseUUIDString), true);
        log.info("Completed Case {}", caseUUIDString);
    }

    public void updateDeadline(String caseUUIDString, String stageUUIDString, String dateReceived) {
        caseworkClient.updateDateReceived(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), LocalDate.parse(dateReceived));
    }

    public void updatePrimaryCorrespondent(String caseUUIDString, String stageUUIDString, String correspondentUUIDString) {
        caseworkClient.updatePrimaryCorrespondent(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), UUID.fromString(correspondentUUIDString));
        log.info("Updated Primary Correspondent for Case {}", caseUUIDString);
    }

    public void updateRegionForPrimaryCorrespondent(String caseUUIDString, String stageUUIDString) {
        UUID regionUUID = caseworkClient.getRegionForCase(UUID.fromString(caseUUIDString));
        updateRegion(caseUUIDString, stageUUIDString, regionUUID == null ? null : regionUUID.toString());
    }

    public void updateRegion(String caseUUIDString, String stageUUIDString, String regionUUIDString) {

        log.info("Update Case {} with Region {}", caseUUIDString, regionUUIDString);
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> data = new HashMap<>();

        log.info("Populating Region with {}", regionUUIDString);
        data.put("RegionUUID", regionUUIDString);

        camundaClient.updateTask(stageUUID, data);
        caseworkClient.updateCase(caseUUID, stageUUID, data);
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

    public void updateTeamForRegionAndStage(String caseUUIDString, String stageUUIDString, String regionUUIDString, String stageType, String teamNameKey, String teamUUIDKey) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID regionUUID = UUID.fromString(regionUUIDString);

        Map<String, String> data = new HashMap<>();
        TeamDto teamDto = infoClient.getTeamForRegionAndStage(caseUUID, regionUUID, stageType);
        data.put(teamNameKey, teamDto.getUuid().toString());
        data.put(teamUUIDKey, teamDto.getDisplayName());

        camundaClient.updateTask(stageUUID, data);
        caseworkClient.updateCase(caseUUID, stageUUID, data);
    }

    public void updateTeamForConstituencyAndStage(String caseUUIDString, String stageUUIDString, String constituencyId, String stageType, String teamNameKey, String teamUUIDKey){
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID constituencyUUID = UUID.fromString(constituencyId);

        Map<String, String> teams = new HashMap<>();
        TeamDto teamDto = infoClient.getTeamForConstituencyAndStage(caseUUID, constituencyUUID, stageType);
        teams.put(teamNameKey, teamDto.getUuid().toString());
        teams.put(teamUUIDKey, teamDto.getDisplayName());

        camundaClient.updateTask(stageUUID, teams);
        caseworkClient.updateCase(caseUUID, stageUUID, teams);
    }

    public void updateTeamSelection(String caseUUIDString, String stageUUIDString, String draftingUUIDString, String privateOfficeUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();

        if(draftingUUIDString != null) {
            log.info("Overwriting draft team with {}", draftingUUIDString);
            TeamDto draftingTeam = infoClient.getTeam(UUID.fromString(draftingUUIDString));
            teamsForTopic.put("DraftingTeamUUID", draftingTeam.getUuid().toString());
            teamsForTopic.put("DraftingTeamName", draftingTeam.getDisplayName());
        }

        if(privateOfficeUUIDString != null) {
            log.info("Overwriting po team with {}", privateOfficeUUIDString);
            TeamDto pOTeam = infoClient.getTeam(UUID.fromString(privateOfficeUUIDString));
            teamsForTopic.put("POTeamUUID", pOTeam.getUuid().toString());
            teamsForTopic.put("POTeamName", pOTeam.getDisplayName());
        }

        if(!teamsForTopic.isEmpty()) {
            camundaClient.updateTask(stageUUID, teamsForTopic);
            caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
        }

        log.debug("######## Updated Team Selection ########");
    }

    public void updatePOTeamSelection(String caseUUIDString, String stageUUIDString, String privateOfficeUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();

        if(privateOfficeUUIDString != null) {
            log.info("Overwriting po team with {}", privateOfficeUUIDString);
            TeamDto pOTeam = infoClient.getTeam(UUID.fromString(privateOfficeUUIDString));
            teamsForTopic.put("PrivateOfficeOverridePOTeamUUID", pOTeam.getUuid().toString());
            teamsForTopic.put("PrivateOfficeOverridePOTeamName", pOTeam.getDisplayName());
        }

        if(!teamsForTopic.isEmpty()) {
            camundaClient.updateTask(stageUUID, teamsForTopic);
            caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
        }

        log.debug("######## Updated Team Selection at PO ########");
    }

    public void updateTeamSelectionByTeamId(String caseUUIDString, String stageUUIDString, String teamUUID, String teamNameProperty, String teamUUIDProperty) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> data = new HashMap<>();

        if(teamUUID != null) {
            log.info("Updating {} team variable with {}", teamUUIDProperty, teamUUID);
            TeamDto team = infoClient.getTeam(UUID.fromString(teamUUID));
            data.put(teamNameProperty, team.getDisplayName());
            data.put(teamUUIDProperty, team.getUuid().toString());
        }

        if(!data.isEmpty()) {
            camundaClient.updateTask(stageUUID, data);
            caseworkClient.updateCase(caseUUID, stageUUID, data);
        }

        log.debug("######## Updated Team Selection By Team Id ########");
    }

    public void updateAllocationNote(String caseUUIDString, String stageUUIDString, String allocationNote, String allocationNoteType) {
        log.debug("######## Save Allocation Note ########");
        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), allocationNoteType, allocationNote);
        log.info("Adding Casenote to Case: {}", caseUUIDString);
    }

}