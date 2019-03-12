package uk.gov.digital.ho.hocs.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.security.AccessLevel;
import uk.gov.digital.ho.hocs.workflow.security.Allocated;
import uk.gov.digital.ho.hocs.workflow.security.AllocationLevel;
import uk.gov.digital.ho.hocs.workflow.security.Authorised;

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

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CreateCaseResponse response = workflowService.createCase(request.getType(), request.getDateReceived(), request.getDocuments());
        return ResponseEntity.ok(response);
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/bulk", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateBulkCaseResponse> createCaseBulk(@RequestBody CreateCaseWithDocumentsRequest request) {
        CaseDataType type = request.getCaseDataType();
        List<DocumentSummary> list = request.getDocuments();
        list.forEach( (documentSummary) -> {
            workflowService.createCase(type, request.getDateReceived(), Collections.singletonList(documentSummary));
        });
        return ResponseEntity.ok(new CreateBulkCaseResponse(list.size()));
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> updateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AddCaseDataRequest request) {
        GetStageResponse response = workflowService.updateStage(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok(response);
    }

    @Allocated(allocatedTo = AllocationLevel.TEAM_USER)
    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(response);
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/{caseUUID}/document", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createDocument(@PathVariable UUID caseUUID,@RequestBody CreateDocumentRequest request) {
        workflowService.createDocument(caseUUID, request.getDocuments());
        return ResponseEntity.ok().build();
    }
}
