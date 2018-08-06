package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.dto.WorkflowType;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.List;
import java.util.UUID;


import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;


@RestController
@Slf4j
class WorkflowResource {

    private WorkflowService workflowService;

    @Autowired
    public WorkflowResource(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @RequestMapping(value = "/caseType", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetWorkflowTypesResponse> getAllCaseTypes() {
        List<WorkflowType> workflowTypes = workflowService.getAllWorkflowTypes();
        return ResponseEntity.ok(new GetWorkflowTypesResponse(workflowTypes));
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CreateCaseResponse response = workflowService.createNewCase(request.getType(), request.getDocuments());
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/case/bulk", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCaseBulk(@RequestBody CreateCaseWithDocumentsRequest request) {
        CaseType type = request.getCaseType();
        List<DocumentSummary> list = request.getDocuments();
        list.forEach( (document) -> {
            workflowService.createNewCase(type, document);
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> submitStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AddCaseDataRequest request) {
        GetStageResponse response = workflowService.updateStage(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) throws EntityCreationException, EntityNotFoundException {
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(response);
    }

}