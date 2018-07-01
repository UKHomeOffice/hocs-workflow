package uk.gov.digital.ho.hocs.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CaseworkDocumentSummary;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private static List<WorkflowType> caseTypeDetails = new ArrayList<>();

    private final CaseworkClient caseworkClient;
    private final CamundaClient camundaClient;

    static {
        caseTypeDetails.add(new WorkflowType("DCU", "DCU MIN", CaseType.MIN));
        caseTypeDetails.add(new WorkflowType("DCU", "DCU TRO", CaseType.TRO));
        caseTypeDetails.add(new WorkflowType("DCU", "DCU DTEN", CaseType.DTEN));
        caseTypeDetails.add(new WorkflowType("UKVI", "UKVI BREF", CaseType.BREF));
    }

    @Autowired
    public WorkflowService(CaseworkClient casework, CamundaClient camunda) {
        caseworkClient = casework;
        camundaClient = camunda;
    }

    public List<WorkflowType> getAllWorkflowTypes() {
        if(caseTypeDetails != null && !caseTypeDetails.isEmpty()){
            return caseTypeDetails;
        } else {
            return new ArrayList<>();
        }
    }

    public uk.gov.digital.ho.hocs.workflow.dto.CreateCaseResponse createNewCase(CaseType caseType) throws EntityCreationException, EntityNotFoundException {
        if (caseType != null) {
            UUID caseUUID = UUID.randomUUID();

            String caseReference =  caseworkClient.createCase(caseUUID, caseType);
            camundaClient.startCase(caseUUID, caseType);

            return new CreateCaseResponse(caseReference, caseUUID);
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    public GetStageScreenResponse startCase(UUID caseUUID) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null) {
            UUID stageUUID = UUID.randomUUID();
            StageType stageType = camundaClient.getCaseStage(caseUUID);

            caseworkClient.createStage(caseUUID, stageUUID, stageType);
            String screenName = camundaClient.startStage(stageUUID, stageType);

            return new GetStageScreenResponse(stageUUID, screenName);
        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
    }

    public GetStageScreenResponse updateCase(UUID caseUUID, UUID stageUUID, Map<String,Object> values) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null && stageUUID != null && values != null) {
            String screenName = camundaClient.validateStage(stageUUID, values);
            return new GetStageScreenResponse(stageUUID, screenName);
        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID, stageUUID or Map!");
        }
    }

    public void addDocuments(UUID caseUUID, List<DocumentSummary> documentSummaryList) throws EntityCreationException {

        List<CaseworkDocumentSummary> caseworkDocumentSummaryList = documentSummaryList.stream().map(CaseworkDocumentSummary::from).collect(Collectors.toList());
        caseworkClient.addDocuments(caseUUID, caseworkDocumentSummaryList);

        //TODO: post to queue { caseUUID, docUUID s3UntrustedUrl}

    }

}
