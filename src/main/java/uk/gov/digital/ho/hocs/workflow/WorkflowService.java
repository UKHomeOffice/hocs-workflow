package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.*;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoDeadlines;
import uk.gov.digital.ho.hocs.workflow.model.*;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class WorkflowService implements JavaDelegate {

    // TODO: this should be in the list service
    private static List<WorkflowType> caseTypeDetails = new ArrayList<>();

    static {
        caseTypeDetails.add(new WorkflowType("DCU MIN", CaseType.MIN.toString()));
        caseTypeDetails.add(new WorkflowType("DCU TRO", CaseType.TRO.toString()));
        caseTypeDetails.add(new WorkflowType("DCU DTEN", CaseType.DTEN.toString()));
      //  caseTypeDetails.add(new WorkflowType("UKVI BREF", CaseType.BREF.toString()));
    }

    private final CaseworkClient caseworkClient;
    private final InfoClient infoClient;
    private final CamundaClient camundaClient;
    private final HocsFormService hocsFormService;

    @Autowired
    public WorkflowService(CaseworkClient caseworkClient, InfoClient infoClient, CamundaClient camundaClient, HocsFormService hocsFormService) {
        this.caseworkClient = caseworkClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
        this.hocsFormService = hocsFormService;
    }

    List<WorkflowType> getAllWorkflowTypes() {
        if(caseTypeDetails != null && !caseTypeDetails.isEmpty()){
            return caseTypeDetails;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void execute(DelegateExecution execution){
    }

    public CreateCaseResponse createCase(CaseType caseType, LocalDate dateReceived, List<DocumentSummary> documents) {
        // Create a case in the casework service in order to get a UUID.
        CwCreateCaseResponse caseResponse = caseworkClient.createCase(caseType, dateReceived);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {
            // Start a new case level workflow (caseUUID is the business key).
            Map<String, Object> seedData = new HashMap<>();
            seedData.put("DateReceived", dateReceived);

            seedData.put("DataInputTeamUUID", "22222222-2222-2222-2222-222222222222");
            seedData.put("DataInputQATeamUUID", "22222222-2222-2222-2222-222222222222");
            seedData.put("MarkupTeamUUID", "11111111-1111-1111-1111-111111111111");
            seedData.put("TransferConfirmationTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("NoReplyNeededTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("InitialDraftTeamUUID", "33333333-3333-3333-3333-333333333333");
            camundaClient.startCase(caseUUID, caseType, seedData);
            if(documents != null) {
                // Add any Documents to the case
                for (DocumentSummary document : documents) {
                    CwCreateDocumentResponse response = caseworkClient.addDocument(caseUUID, document.getDisplayName(), document.getType());
                    //TODO: post to queue (response.getUuid(), documentSummary.getS3UntrustedUrl());
                }
            }
        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
        return new CreateCaseResponse(caseUUID,caseResponse.getReference());
    }

    public void calculateDeadlines(String caseUUIDString,String caseTypeString, String dateReceivedString) {
        log.debug("######## Calculating Deadlines ########");
        UUID caseUUID = UUID.fromString(caseUUIDString);
        LocalDate now = LocalDate.parse(dateReceivedString);
        CaseType caseType = CaseType.valueOf(caseTypeString);
        Set<InfoDeadlines> deadlines = infoClient.getDeadlines(caseType, now);
        caseworkClient.setDeadlines(caseUUID, deadlines);
        log.debug("######## Created Stage ########");
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageType, String teamUUIDString, String userUUIDString) {
        log.debug("######## Creating Stage ########");

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID;
        UUID teamUUID = null;
        UUID userUUID = null;

        if(teamUUIDString != null) {
            teamUUID = UUID.fromString(teamUUIDString);
        }
        if(userUUIDString != null) {
            userUUID = UUID.fromString(userUUIDString);
        }

        if(stageUUIDString != null) {
            // Otherwise just allocate the stage.
            stageUUID = UUID.fromString(stageUUIDString);

            caseworkClient.allocateStage(caseUUID, stageUUID, teamUUID, userUUID);
        } else {
            // Create a stage in the casework service in order to get a UUID.
            CwCreateStageResponse stageResponse = caseworkClient.createStage(caseUUID, StageType.valueOf(stageType), teamUUID, userUUID);
            stageUUID = stageResponse.getUuid();
        }
        log.debug("######## Created Stage ########");
        return stageUUID.toString();
    }

    public GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getScreenName(stageUUID);
        // TODO: replace Hocs form service with calls to list service.
        HocsForm form = hocsFormService.getForm(screenName);
        // If the stage is complete we have form as null.
        if(form != null) {
            // TODO: permission check (active stage userID? TeamID ?)
            CwGetStageResponse response = caseworkClient.getStage(caseUUID, stageUUID);
            form.setData(response.getData());
            return new GetStageResponse(stageUUID, response.getCaseReference(), form);
        }
        else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    public GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String,String> values){
        // TODO: permission check (active stage userID? TeamID ?)
        // TODO: validate Form
        caseworkClient.updateStage(caseUUID,stageUUID, values);
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
}