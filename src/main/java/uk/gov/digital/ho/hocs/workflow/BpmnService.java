package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

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
        log.debug("Creating or Updating Stage {}", stageTypeString);

        UUID teamUUID;
        if(allocationTeamString == null) {
            teamUUID = infoClient.getTeamForStageType(stageTypeString);
        } else {
            log.info("Overriding Team selection with {}", allocationTeamString);
            teamUUID = UUID.fromString(allocationTeamString);
        }

        if (stageUUIDString != null) {
            log.info("Stage {} already exists for case {}, assigning to team {}", stageTypeString, caseUUIDString, teamUUID);
            caseworkClient.updateStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), teamUUID, allocationType);
            return stageUUIDString;
        } else {
            log.info("Creating new stage {} for case {}, assigning to team {}", stageTypeString, caseUUIDString, teamUUID);
            return caseworkClient.createStage(UUID.fromString(caseUUIDString), stageTypeString, teamUUID, allocationType).toString();
        }
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        caseworkClient.updateStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), null, null);
        log.debug("######## Updated Stage ########");
    }

    public void updatePrimaryCorrespondent(String caseUUIDString, String stageUUIDString, String correspondentUUIDString) {
        caseworkClient.updatePrimaryCorrespondent(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), UUID.fromString(correspondentUUIDString));
        log.debug("######## Updated Primary Correspondent ########");
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

    public void updateAllocationNote(String caseUUIDString, String stageUUIDString, String allocationNote, String allocationNoteType) {
        log.debug("######## Save Allocation Note ########");
        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), allocationNoteType, allocationNote);
        log.info("Adding Casenote to Case: {}", caseUUIDString);
    }

}