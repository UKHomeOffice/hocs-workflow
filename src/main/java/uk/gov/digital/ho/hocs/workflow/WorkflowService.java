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

            // Create a case in the workflow service in order to get a UUID
            CwCreateCaseResponse response = caseworkClient.createCase(caseType);
            UUID caseUUID = response.getUuid();

            // As this is a brand new case start a new workflow
            camundaClient.startCase(caseUUID, caseType);

            //
            startStage(caseUUID);

            return new CreateCaseResponse(caseUUID,response.getReference());
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

    private void addDocument(UUID caseUUID, String displayName, DocumentType type) throws EntityCreationException {

        CwCreateDocumentResponse response = caseworkClient.addDocument(caseUUID, displayName, type);

        //TODO: post to queue (response.getUuid(), documentSummary.getS3UntrustedUrl());
    }

    private void startStage(UUID caseUUID) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null) {
            StageType stageType = camundaClient.getCaseStage(caseUUID);

            CwCreateStageResponse response = caseworkClient.createStage(caseUUID, stageType);
            UUID stageUUID = response.getUuid();
            camundaClient.startStage(stageUUID, stageType);

        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
    }

}
