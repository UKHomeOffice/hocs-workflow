package uk.gov.digital.ho.hocs.workflow;

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
public class WorkflowService {

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

    CreateCaseResponse createNewCase(CaseType caseType, List<DocumentSummary> documents) throws EntityCreationException, EntityNotFoundException {
        if (caseType != null) {

            // Create a case in the casework service in order to get a UUID.
            CwCreateCaseResponse caseResponse = caseworkClient.createCase(caseType);
            UUID caseUUID = caseResponse.getUuid();

            if (caseUUID != null) {

                // Start a new case level workflow (caseUUID is the business key).
                StageType stageType = camundaClient.startCase(caseUUID, caseType);

                // Create a stage in the casework service in order to get a UUID.
                CwCreateStageResponse stageResponse = caseworkClient.createStage(caseUUID, stageType);
                UUID stageUUID = stageResponse.getUuid();

                if(stageUUID != null) {

                    // Start a new stage level workflow (stage UUID is the business key).
                    camundaClient.startStage(stageUUID, stageType);
                }
                else {
                    throw new EntityCreationException("Failed to start case, invalid stageUUID!");

                    // TODO: if this fails we should tidy up here.
                }

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

    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null) {
            String screenName = camundaClient.getCurrentScreen(stageUUID);
            HocsForm form = hocsFormService.getStage(screenName);
            return new GetStageResponse(stageUUID,"Dummy Case Ref", screenName, form);
        } else {
            throw new EntityCreationException("Failed to Get case, invalid caseUUID or stageUUID!");
        }
    }

    GetStageResponse updateCase(UUID caseUUID, UUID stageUUID, Map<String,Object> values) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null && values != null) {
            String screenName = camundaClient.updateStage(stageUUID, values);
            HocsForm form = hocsFormService.getStage(screenName);
            return new GetStageResponse(stageUUID,"Dummy Case Ref", screenName, form);
        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID, stageUUID or values!");
        }
    }

    private void addDocument(UUID caseUUID, DocumentSummary document) throws EntityCreationException {

        CwCreateDocumentResponse response = caseworkClient.addDocument(caseUUID, document.getDisplayName(), document.getType());

        //TODO: post to queue (response.getUuid(), documentSummary.getS3UntrustedUrl());
    }
}
