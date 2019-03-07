package uk.gov.digital.ho.hocs.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@Slf4j
class MigrationWorkflowResource {

    private MigrationWorkflowService workflowService;

    @Autowired
    public MigrationWorkflowResource(MigrationWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping(value = "migration/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<MigrationCreateCaseResponse> createCase(@RequestBody MigrationCreateCaseRequest request) {
        MigrationCreateCaseResponse response = workflowService.createCase(request.getType(), request.getCaseReference(), request.getDateReceived(), request.getCaseDeadline(), request.getData(), request.getTopic());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "migration/case/progress", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<MigrationProgressCaseResponse> createCase(@RequestBody MigrationProgressCaseRequest request) {
        MigrationProgressCaseResponse response = workflowService.progressCase(request.getCaseUUID(), request.getType(),  request.getData(), request.getSeedData(), request.getCorrespondent(), request.getDraftDocumentUUID(), request.getTopic());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "migration/case/{caseUUID}/document", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UUID> createDocument(@PathVariable UUID caseUUID,@RequestBody CreateDocumentRequest request) {
        workflowService.createDocument(caseUUID, request.getDocuments());
        return ResponseEntity.ok().build();
    }
}