package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.Deadline;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;

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

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String dateReceivedString, String allocationType, String allocationTeamString) {

        UUID caseUUID = UUID.fromString(caseUUIDString);

        UUID teamUUID;
        if(allocationTeamString == null) {
            teamUUID = infoClient.getTeam(stageTypeString);
        } else {
            teamUUID = UUID.fromString(allocationTeamString);
        }

        if (stageUUIDString != null) {
            // This happens on a reject, so we need to update the team.
            caseworkClient.updateStageTeam(caseUUID, UUID.fromString(stageUUIDString), teamUUID, allocationType);
            return stageUUIDString;
        } else {
            LocalDate dateReceived = LocalDate.parse(dateReceivedString);
            Deadline deadline = infoClient.getDeadline(StageType.valueOf(stageTypeString), dateReceived);
            return caseworkClient.createStage(caseUUID, StageType.valueOf(stageTypeString), teamUUID, deadline.getDate(), allocationType).toString();
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

        Map<String, String> teamsForTopic = new HashMap<>();
        TeamDto draftingTeam = infoClient.getTeamForTopicAndStage(caseUUID, topicUUID, "DCU_MIN_INITIAL_DRAFT");
        TeamDto pOTeam = infoClient.getTeamForTopicAndStage(caseUUID, topicUUID, "DCU_MIN_PRIVATE_OFFICE");
        teamsForTopic.put("DraftingTeamUUID", draftingTeam.getUuid().toString());
        teamsForTopic.put("DraftingTeamName", draftingTeam.getDisplayName());
        teamsForTopic.put("POTeamUUID", pOTeam.getUuid().toString());
        teamsForTopic.put("POTeamName", pOTeam.getDisplayName());
        camundaClient.updateTask(stageUUID, teamsForTopic);
        caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);

        log.debug("######## Updated Primary Topic ########");
    }

    public void updateAllocationNote(String caseUUIDString, String stageUUIDString, String allocationNote) {

        //caseworkClient.updatePrimaryTopic(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), UUID.fromString(topicUUIDString));
        log.debug("######## Save Allocation Note ########");
    }

}