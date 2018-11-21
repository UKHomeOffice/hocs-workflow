package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.Deadline;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;
import uk.gov.digital.ho.hocs.workflow.client.notificationclient.EmailService;
import uk.gov.digital.ho.hocs.workflow.client.notificationclient.NotifyType;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class BpmnService {

    private final CaseworkClient caseworkClient;
    private final InfoClient infoClient;
    private final EmailService emailService;

    @Autowired
    public BpmnService(CaseworkClient caseworkClient,
                       InfoClient infoClient,
                       EmailService emailService) {
        this.caseworkClient = caseworkClient;
        this.infoClient = infoClient;
        this.emailService = emailService;
    }

    public void addCaseNote(String caseUUIDString, String caseNote){
        UUID caseUUID = UUID.fromString(caseUUIDString);
        caseworkClient.createCaseNote(caseUUID, caseNote, CaseNoteType.MANUAL);
        log.debug("######## Added case note ########");
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String dateReceivedString, String teamUUIDString, String userUUIDString) {

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID;
        UUID teamUUID = null;
        UUID userUUID = null;

        if (teamUUIDString != null) {
            teamUUID = UUID.fromString(teamUUIDString);
        }
        if (userUUIDString != null) {
            userUUID = UUID.fromString(userUUIDString);
        }

        if (stageUUIDString != null) {
            // Otherwise just allocate the stage.
            stageUUID = UUID.fromString(stageUUIDString);

            // TODO: look at how we can give a better stage status
            caseworkClient.updateStage(caseUUID, stageUUID, teamUUID, userUUID, StageStatusType.UPDATED);
        } else {
            // Create a stage in the casework service in order to get a UUID.
            LocalDate now = LocalDate.parse(dateReceivedString);

            Deadline deadline = infoClient.getDeadline(StageType.valueOf(stageTypeString), now);
            stageUUID = caseworkClient.createStage(caseUUID, StageType.valueOf(stageTypeString), teamUUID, userUUID, deadline.getDate());
        }
        log.debug("######## Created Stage ########");
        return stageUUID.toString();
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        caseworkClient.updateStage(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), null, null, StageStatusType.COMPLETED);
        log.debug("######## Updated Stage ########");
    }

    public void sendEmail(String caseUUIDString, String caseRef, String stageUUIDString, String teamUUIDString, NotifyType notifyType) {
        emailService.sendEmail(caseUUIDString,caseRef,stageUUIDString, teamUUIDString, notifyType);
        log.debug("######## Sent {} Email ########", notifyType);
    }

}