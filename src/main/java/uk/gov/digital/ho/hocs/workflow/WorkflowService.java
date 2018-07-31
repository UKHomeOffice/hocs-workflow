package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
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

        int fsdfsd = 4;
    }


    public String createStage(String caseUUIDString, String type) throws EntityCreationException {
        log.debug("######## Creating Stage ########");
        UUID caseUUID = UUID.fromString(caseUUIDString);
        StageType stageType = StageType.valueOf(type);

        // Create a stage in the casework service in order to get a UUID.
        CwCreateStageResponse stageResponse = caseworkClient.createStage(caseUUID, stageType);
        UUID stageUUID = stageResponse.getUuid();

        log.debug("######## Created Stage ########");
        return stageUUID.toString();
    }

    public void updateStage(String caseUUIDString, String stageUUIDString) throws EntityCreationException {
        log.debug("######## Updating Stage ########");

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        // Finish the stage.
        caseworkClient.completeStage(caseUUID, stageUUID);
        log.debug("######## Updated Stage ########");
    }

    public void allocateStage(String caseUUIDString, String stageUUIDString) throws EntityCreationException {
        log.debug("######## Allocating Stage ########");

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        caseworkClient.allocateStage(caseUUID, stageUUID);
        log.debug("######## Allocated Stage ########");
    }

    public void crateDeadlines(){
        log.debug("######## Creating Deadline ########");
        // Find the deadlines for casetype
        // INFO_SERVICE/deadlines { caseType: MIN, date: 2018-12-20 }
        //deadlines[] { stageType, date }
        // OR INFO_SERVICE/casetype/MIN/datereceived/2018-12-20 ??

        // INFO_SERVICE/casetype/COL/datereceived/2018-12-20/
        //{stageType, date }

        //{
        //    casetype: MIN,
        //    date: today,
        //    slas :[{ dispatch: 10/5, qa: 5, draft: 7 }]
        //}

        log.debug("######## Created Deadline ########");

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

    CreateCaseResponse createNewCase(CaseType caseType, DocumentSummary documentSummary) throws EntityNotFoundException, EntityCreationException {
        return createNewCase(caseType, Collections.singletonList(documentSummary));
    }

    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null) {
            // TODO: get Active Stage + Stage Data
            // TODO: permission check
            String screenName = camundaClient.getCurrentScreen(stageUUID);
            HocsForm form = hocsFormService.getStageForm(screenName);
            // TODO: populate schema
            return new GetStageResponse(stageUUID,"Dummy Case Ref", screenName, form);
        } else {
            throw new EntityCreationException("Failed to Get case, invalid caseUUID or stageUUID!");
        }
    }

    GetStageResponse updateCase(UUID caseUUID, UUID stageUUID, Map<String,String> values) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null && values != null) {
            // TODO: get Active Stage + Stage Data
            // TODO: permission check
            String screenName = camundaClient.getCurrentScreen(stageUUID);
            HocsForm form = hocsFormService.getStageForm(screenName);
            //TODO: validate Form
            caseworkClient.updateStage(caseUUID,stageUUID, screenName, values);
            String newScreenName = camundaClient.updateStage(stageUUID, values);
            log.debug("Updating Case: '{}', Stage: '{}' NextScreen: '{}'", caseUUID, stageUUID, newScreenName);
            HocsForm newForm = hocsFormService.getStageForm(newScreenName);
            return new GetStageResponse(stageUUID,"Dummy Case Ref", newScreenName, newForm);
        } else {
            throw new EntityCreationException("Failed to update case, invalid caseUUID, stageUUID or values!");
        }
    }

    private void addDocument(UUID caseUUID, DocumentSummary document) throws EntityCreationException {

        CwCreateDocumentResponse response = caseworkClient.addDocument(caseUUID, document.getDisplayName(), document.getType());

        //TODO: post to queue (response.getUuid(), documentSummary.getS3UntrustedUrl());
    }
}
