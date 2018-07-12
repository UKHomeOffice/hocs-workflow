package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CwCreateCaseResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CwCreateDocumentResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CwCreateStageResponse;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.*;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;

import java.util.*;

@Service
@Slf4j
public class WorkflowService implements JavaDelegate {

    private static List<WorkflowType> caseTypeDetails = new ArrayList<>();

    private final CaseworkClient caseworkClient;
    private final CamundaClient camundaClient;
    private final HocsFormService hocsFormService;

    static {
        caseTypeDetails.add(new WorkflowType("DCU", "DCU MIN", CaseType.MIN));
        caseTypeDetails.add(new WorkflowType("DCU", "DCU TRO", CaseType.TRO));
        caseTypeDetails.add(new WorkflowType("DCU", "DCU DTEN", CaseType.DTEN));
        caseTypeDetails.add(new WorkflowType("UKVI", "UKVI BREF", CaseType.BREF));
    }

    @Autowired
    public WorkflowService(CaseworkClient caseworkClient, CamundaClient camundaClient, HocsFormService hocsFormService) {
        this.caseworkClient = caseworkClient;
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


    public String createStage(String uuid, String type) throws EntityCreationException {
        UUID caseUUID = UUID.fromString(uuid);
        StageType stageType = StageType.valueOf(type);

        // Create a stage in the casework service in order to get a UUID.
        CwCreateStageResponse stageResponse = caseworkClient.createStage(caseUUID, stageType);
        UUID stageUUID = stageResponse.getUuid();
        return stageUUID.toString();
    }

    public void updateStage(String caseString, String stageString) throws EntityCreationException {
        UUID caseUUID = UUID.fromString(caseString);
        UUID stageUUID = UUID.fromString(stageString);

        // Finish the stage.
        caseworkClient.updateStage(caseUUID, stageUUID);
    }

    CreateCaseResponse createNewCase(CaseType caseType, List<DocumentSummary> documents) throws EntityCreationException, EntityNotFoundException {
        if (caseType != null) {

            // Create a case in the casework service in order to get a UUID.
            CwCreateCaseResponse caseResponse = caseworkClient.createCase(caseType);
            UUID caseUUID = caseResponse.getUuid();

            if (caseUUID != null) {

                // Start a new case level workflow (caseUUID is the business key).
                camundaClient.startCase(caseUUID, caseType);

            } else {
                throw new EntityCreationException("Failed to start case, invalid caseUUID!");

                // TODO: if this fails we should tidy up here.
            }

            // Add any Documents to the case
            if(documents != null) {
                for (DocumentSummary document : documents) {
                    addDocument(caseUUID, document);
                }
            }

            return new CreateCaseResponse(caseUUID,caseResponse.getReference());
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    CreateCaseResponse createNewCase(CaseType caseType, DocumentSummary documentSummary) throws EntityNotFoundException, EntityCreationException {
        return createNewCase(caseType, Collections.singletonList(documentSummary));
    }

    CreateCaseResponse createNewCase(CaseType caseType, List<DocumentSummary> documentSummaries) throws EntityCreationException, EntityNotFoundException {
        CreateCaseResponse newCase = createNewCase(caseType);
        addDocuments(newCase.getUuid(), documentSummaries);
        return newCase;
    }
    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null) {
            String screenName = camundaClient.getCurrentScreen(stageUUID);
            HocsForm form = hocsFormService.getStageForm(screenName);
            return new GetStageResponse(stageUUID,"Dummy Case Ref", screenName, form);
        } else {
            throw new EntityCreationException("Failed to Get case, invalid caseUUID or stageUUID!");
        }
    }

    GetStageResponse updateCase(UUID caseUUID, UUID stageUUID, Map<String,String> values) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null && values != null) {
            String screenName = camundaClient.getCurrentScreen(stageUUID);
            caseworkClient.createScreen(caseUUID,stageUUID, screenName, values);
            String newScreenName = camundaClient.updateStage(stageUUID, values);
            HocsForm form = hocsFormService.getStageForm(newScreenName);
            return new GetStageResponse(stageUUID,"Dummy Case Ref", newScreenName, form);
        } else {
            throw new EntityCreationException("Failed to update case, invalid caseUUID, stageUUID or values!");
        }
    }

    private void addDocument(UUID caseUUID, DocumentSummary document) throws EntityCreationException {

        CwCreateDocumentResponse response = caseworkClient.addDocument(caseUUID, document.getDisplayName(), document.getType());

        //TODO: post to queue (response.getUuid(), documentSummary.getS3UntrustedUrl());
    }
}
