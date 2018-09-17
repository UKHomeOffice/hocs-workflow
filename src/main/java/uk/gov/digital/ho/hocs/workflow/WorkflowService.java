package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.*;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkCaseTypeResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkInputResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkStageResponse;
import uk.gov.digital.ho.hocs.workflow.documentClient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.model.*;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.notifications.EmailService;
import uk.gov.digital.ho.hocs.workflow.notifications.NotifyType;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class WorkflowService implements JavaDelegate {

    private final CaseworkClient caseworkClient;
    private final DocumentClient documentClient;
    private final InfoClient infoClient;
    private final CamundaClient camundaClient;
    private final HocsFormService hocsFormService;
    private final EmailService emailService;


    @Autowired
    public WorkflowService(CaseworkClient caseworkClient,
                           DocumentClient documentClient,
                           InfoClient infoClient,
                           CamundaClient camundaClient,
                           HocsFormService hocsFormService,
                           EmailService emailService) {
        this.caseworkClient = caseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
        this.hocsFormService = hocsFormService;
        this.emailService = emailService;
    }

    @Override
    public void execute(DelegateExecution execution) {
    }

    public CreateCaseResponse createCase(CaseType caseType, LocalDate dateReceived, List<DocumentSummary> documents) {
        // Create a case in the casework service in order to get a UUID.
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseType);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {
            // Start a new case level workflow (caseUUID is the business key).
            Map<String, Object> seedData = new HashMap<>();
            seedData.put("DateReceived", dateReceived);

            seedData.put("DataInputTeamUUID", "44444444-2222-2222-2222-222222222222");
            seedData.put("DataInputQATeamUUID", "22222222-2222-2222-2222-222222222222");
            seedData.put("MarkupTeamUUID", "11111111-1111-1111-1111-111111111111");
            seedData.put("TransferConfirmationTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("NoReplyNeededTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("InitialDraftTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("QAResponseTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("PrivateOfficeTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("MinisterSignOffTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("DispatchTeamUUID", "33333333-3333-3333-3333-333333333333");

            Map<String, String> data = new HashMap<>();
            data.put("DateReceived", dateReceived.toString());
            caseworkClient.setInputData(caseUUID, data);
            createDocument(caseUUID, documents);
            camundaClient.startCase(caseUUID, caseType, seedData);

        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
        return new CreateCaseResponse(caseUUID, caseResponse.getReference());
    }

    public void createDocument(UUID caseUUID, List<DocumentSummary> documents) {

        if (documents != null) {
            // Add any Documents to the case
            for (DocumentSummary document : documents) {
                UUID response = documentClient.createDocument(caseUUID, document.getDisplayName(), document.getType());

                documentClient.processDocument(caseUUID, response, document.getS3UntrustedUrl());
            }
        }
    }

    public void deleteDocument(UUID caseUUID, UUID documentUUID) {

         documentClient.deleteDocument(caseUUID, documentUUID);

    }


    public void lookupCorrespondent(String caseUUIDString, String CFullName) {
        UUID caseUUID = UUID.fromString(caseUUIDString);

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Lookup full name in Info service.

        //if not null
            // save the correspondent details
    }

    public void clearCorrespondentData(String caseUUIDString) {

        //caseworkClient.
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
        // Do nothing.
        UUID caseUUID = UUID.fromString(caseUUIDString);
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

    // TODO: this is BS but we need to move on.
    public void clearTempData(String caseUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);

        Set<String> dataToRemove = new HashSet<>();

        dataToRemove.add("TEMPCType");
        dataToRemove.add("TEMPCFullName");
        dataToRemove.add("TEMPCPostcode");
        dataToRemove.add("TEMPCAddressOne");
        dataToRemove.add("TEMPCAddressTwo");
        dataToRemove.add("TEMPCCountry");
        dataToRemove.add("TEMPCEmail");
        dataToRemove.add("TEMPCPhone");
        dataToRemove.add("TEMPCAddressThree");
        dataToRemove.add("TEMPCReference");
        dataToRemove.add("TEMPAdditionalCorrespondent");

        caseworkClient.removeInputData(caseUUID, dataToRemove);
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

    public GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getScreenName(stageUUID);
        HocsForm form = hocsFormService.getForm(screenName);

        // If the stage is complete we have form as null.
        if (form != null) {
            // TODO: permission check (active stage userID? TeamID ?)
            GetCaseworkStageResponse stageResponse = caseworkClient.getStage(caseUUID, stageUUID);
            GetCaseworkInputResponse inputResponse = caseworkClient.getInput(caseUUID);
            form.setData(inputResponse.getData());
            return new GetStageResponse(stageUUID, stageResponse.getCaseReference(), form);
        } else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    public GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values) {
        // TODO: permission check (active stage userID? TeamID ?)
        // TODO: validate Form
        caseworkClient.setInputData(caseUUID, values);
        camundaClient.updateStage(stageUUID, values);

        return getStage(caseUUID, stageUUID);
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        log.debug("######## Updating Stage ########");
        caseworkClient.completeStage(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString));
        log.debug("######## Updated Stage ########");
    }

    public void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        camundaClient.allocateStage(caseUUID, teamUUID, userUUID);
        caseworkClient.allocateStage(caseUUID, stageUUID, teamUUID, userUUID);
    }

    public void sendEmail(String caseUUIDString, String caseRef, String stageUUIDString, String teamUUIDString, NotifyType notifyType) {
        log.debug("######## Sending {} Email ########", notifyType);
        //emailService.sendEmail(caseUUIDString,caseRef,stageUUIDString, teamUUIDString, notifyType);
        log.debug("######## Sent {} Email ########", notifyType);
    }

    public GetParentTopicResponse getParentTopics(UUID caseUUID) {
        // TODO: get case type
//        String caseType = "MIN";
        GetCaseworkCaseTypeResponse caseTypeResponse = caseworkClient.getCaseTypeForCase(caseUUID);
        return infoClient.getParentTopics(caseTypeResponse.getType().toString());
    }

    public void addTopicToCase(UUID caseUUID, UUID topicUUID) {
        caseworkClient.addTopicToCase(caseUUID, topicUUID);
    }

    public void deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        caseworkClient.deleteTopicFromCase(caseUUID, topicUUID);
    }
}
