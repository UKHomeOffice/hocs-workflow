package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.Deadline;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class BpmnService {

    private final CaseworkClient caseworkClient;
    private final InfoClient infoClient;

    @Autowired
    public BpmnService(CaseworkClient caseworkClient,
                       InfoClient infoClient) {
        this.caseworkClient = caseworkClient;
        this.infoClient = infoClient;
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String dateReceivedString, String allocationType) {

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID teamUUID = infoClient.getTeam(stageTypeString);

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
        caseworkClient.completeStage(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString));
        log.debug("######## Updated Stage ########");
    }

}