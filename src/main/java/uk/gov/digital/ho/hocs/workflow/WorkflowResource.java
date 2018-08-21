package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.dto.WorkflowType;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.Collections;
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

    @GetMapping(value = "/caseType", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetWorkflowTypesResponse> getAllCaseTypes() {
        List<WorkflowType> workflowTypes = workflowService.getAllWorkflowTypes();
        return ResponseEntity.ok(new GetWorkflowTypesResponse(workflowTypes));
    }

    @PostMapping(value = "/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CreateCaseResponse response = workflowService.createCase(request.getType(), request.getDateReceived(), request.getDocuments());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/case/bulk", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateBulkCaseResponse> createCaseBulk(@RequestBody CreateCaseWithDocumentsRequest request) {
        CaseType type = request.getCaseType();
        List<DocumentSummary> list = request.getDocuments();
        list.forEach( (documentSummary) -> {
            workflowService.createCase(type, request.getDateReceived(), Collections.singletonList(documentSummary));
        });
        return ResponseEntity.ok(new CreateBulkCaseResponse(list.size()));
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> updateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AddCaseDataRequest request) {
        GetStageResponse response = workflowService.updateStage(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/allocate", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AllocateCaseRequest request) {
        workflowService.allocateStage(caseUUID, stageUUID, request.getTeamUUID(), request.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(response);
    }

}