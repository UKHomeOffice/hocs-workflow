package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.model.*;
import uk.gov.digital.ho.hocs.workflow.notifications.EmailService;
import uk.gov.digital.ho.hocs.workflow.notifications.NotifyType;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BpmnService implements JavaDelegate {

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

    @Override
    public void execute(DelegateExecution execution) {
    }

    public void addCorrespondent(String caseUUIDString, String cType, String cFullName, String cPostcode, String cAddressOne, String cAddressTwo, String cAddressThree, String cAddressCountry, String cPhone, String cEmail, String cReference ){
        UUID caseUUID = UUID.fromString(caseUUIDString);

        CorrespondentType correspondentType = CorrespondentType.valueOf(cType);
        Correspondent correspondent = new Correspondent(correspondentType, cFullName, cPostcode, cAddressOne, cAddressTwo, cAddressThree, cAddressCountry, cPhone, cEmail);
        caseworkClient.createCorrespondent(caseUUID, correspondent);

        if(cReference != null) {
            caseworkClient.createReference(caseUUID, ReferenceType.CORESPONDENT_REFERENCE, cReference);
        }
    }

    public void addCaseNote(String caseUUIDString, String caseNote){
        log.debug("######## Add case note ########");
        UUID caseUUID = UUID.fromString(caseUUIDString);
        caseworkClient.createCaseNote(caseUUID, caseNote);
        log.debug("######## Added case note ########");
    }

    public void calculateDeadlines(String caseUUIDString, String caseTypeString, String dateReceivedString) {
        log.debug("######## Calculating Deadlines ########");
        UUID caseUUID = UUID.fromString(caseUUIDString);
        LocalDate now = LocalDate.parse(dateReceivedString);
        CaseType caseType = CaseType.valueOf(caseTypeString);
        Map<StageType, LocalDate> deadlines = infoClient.getDeadlines(caseType, now);
        caseworkClient.createDeadlines(caseUUID, deadlines);
        log.debug("######## Created Stage ########");
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageType, String teamUUIDString, String userUUIDString) {
        log.debug("######## Creating Stage ########");

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

            caseworkClient.allocateStage(caseUUID, stageUUID, teamUUID, userUUID);
        } else {
            // Create a stage in the casework service in order to get a UUID.
            stageUUID = caseworkClient.createStage(caseUUID, StageType.valueOf(stageType), teamUUID, userUUID);
        }
        log.debug("######## Created Stage ########");
        return stageUUID.toString();
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        log.debug("######## Updating Stage ########");
        caseworkClient.completeStage(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString));
        log.debug("######## Updated Stage ########");
    }

    public void sendEmail(String caseUUIDString, String caseRef, String stageUUIDString, String teamUUIDString, NotifyType notifyType) {
        log.debug("######## Sending {} Email ########", notifyType);
        emailService.sendEmail(caseUUIDString,caseRef,stageUUIDString, teamUUIDString, notifyType);
        log.debug("######## Sent {} Email ########", notifyType);
    }

}