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
        caseTypeDetails.add(new WorkflowType("DCU MIN", CaseType.MIN.toString()));
        caseTypeDetails.add(new WorkflowType("DCU TRO", CaseType.TRO.toString()));
        caseTypeDetails.add(new WorkflowType("DCU DTEN", CaseType.DTEN.toString()));
        caseTypeDetails.add(new WorkflowType("UKVI BREF", CaseType.BREF.toString()));
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

    public void endStage(String caseUUIDString, String stageUUIDString) throws EntityCreationException {
        log.debug("######## Updating Stage ########");

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        // Finish the stage.
        caseworkClient.completeStage(caseUUID, stageUUID);
        log.debug("######## Updated Stage ########");
    }

    public String beginStage(String caseUUIDString, String stageUUIDString, String stageType) throws EntityCreationException {
        log.debug("######## Creating Stage ########");
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID;

        if(stageUUIDString == null) {
            // Create a stage in the casework service in order to get a UUID.
            CwCreateStageResponse stageResponse = caseworkClient.createStage(caseUUID, StageType.valueOf(stageType));
            stageUUID = stageResponse.getUuid();

        } else {
            stageUUID = UUID.fromString(stageUUIDString);
            caseworkClient.allocateStage(caseUUID, stageUUID);
        }

        log.debug("######## Created Stage ########");
        return stageUUID.toString();
    }

    CreateCaseResponse createNewCase(CaseType caseType, DocumentSummary documentSummary) throws EntityNotFoundException, EntityCreationException {
        return createNewCase(caseType, Collections.singletonList(documentSummary));
    }

    CreateCaseResponse createNewCase(CaseType caseType, List<DocumentSummary> documents) throws EntityCreationException, EntityNotFoundException {
        if (caseType != null) {

            // Create a case in the casework service in order to get a UUID.
            CwCreateCaseResponse caseResponse = caseworkClient.createCase(caseType);
            UUID caseUUID = caseResponse.getUuid();

            if (caseUUID != null) {

                // Add any Documents to the case
                if(documents != null) {
                    for (DocumentSummary document : documents) {
                        addDocument(caseUUID, document);
                    }
                }

                // Start a new case level workflow (caseUUID is the business key).
                camundaClient.startCase(caseUUID, caseType);

            } else {
                throw new EntityCreationException("Failed to start case, invalid caseUUID!");

                // TODO: if this fails we should tidy up here.
            }

            return new CreateCaseResponse(caseUUID,caseResponse.getReference());
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        // TODO: permission check (active stage userID? TeamID ?)
        CwGetStageResponse response = caseworkClient.getStage(caseUUID, stageUUID);
        HocsForm form = getFormForStage(stageUUID);
        if(form != null) {
            // If the stage is finished we have no form.
            form.setData(response.getData());
        }
        return new GetStageResponse(stageUUID,response.getCaseReference(), form);
    }

    GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String,String> values){
        // TODO: permission check (active stage userID? TeamID ?)
        log.debug("Updating Case: '{}', Stage: '{}'", caseUUID, stageUUID);

        //TODO: validate Form
        caseworkClient.updateStage(caseUUID,stageUUID, values);
        camundaClient.updateStage(stageUUID, values);

        return getStage(caseUUID, stageUUID);
    }

    private HocsForm getFormForStage(UUID stageUUID) {
        String screenName = camundaClient.getScreenName(stageUUID);
       return hocsFormService.getForm(screenName);
    }

    private void addDocument(UUID caseUUID, DocumentSummary document) throws EntityCreationException {

        CwCreateDocumentResponse response = caseworkClient.addDocument(caseUUID, document.getDisplayName(), document.getType());

        //TODO: post to queue (response.getUuid(), documentSummary.getS3UntrustedUrl());
    }
}
