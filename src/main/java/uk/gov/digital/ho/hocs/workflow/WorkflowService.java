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
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;

import java.util.*;
import java.util.stream.Collectors;

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

    CreateCaseResponse createNewCase(CaseType caseType) throws EntityCreationException, EntityNotFoundException {
        if (caseType != null) {
            UUID caseUUID = UUID.randomUUID();

            String caseReference = caseworkClient.createCase(caseUUID, caseType);
            camundaClient.startCase(caseUUID, caseType);
            startCase(caseUUID);

            return new CreateCaseResponse(caseReference, caseUUID);
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
            throw new EntityCreationException("Failed to start case, invalid caseUUID, stageUUID or Map!");
        }
    }

    void addDocuments(UUID caseUUID, List<DocumentSummary> documentSummaryList) throws EntityCreationException {
        List<CaseworkDocumentSummary> caseworkDocumentSummaryList = documentSummaryList.stream().map(CaseworkDocumentSummary::from).collect(Collectors.toList());
        caseworkClient.addDocuments(caseUUID, caseworkDocumentSummaryList);

        //TODO: post to queue { caseUUID, docUUID s3UntrustedUrl}
    }

    private void startCase(UUID caseUUID) throws EntityNotFoundException, EntityCreationException {
        if (caseUUID != null) {
            UUID stageUUID = UUID.randomUUID();
            StageType stageType = camundaClient.getCaseStage(caseUUID);

            caseworkClient.createStage(caseUUID, stageUUID, stageType);
            camundaClient.startStage(stageUUID, stageType);

        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
    }

}
