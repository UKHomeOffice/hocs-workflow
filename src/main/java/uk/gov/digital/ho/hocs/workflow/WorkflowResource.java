package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.dto.WorkflowType;

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

    @RequestMapping(value = "/workflow", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetWorkflowTypesResponse> getAllWorkflowTypes() {
        List<WorkflowType> workflowTypes = workflowService.getAllWorkflowTypes();
        return ResponseEntity.ok(new GetWorkflowTypesResponse(workflowTypes));
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        try {
            CreateCaseResponse response = workflowService.createNewCase(request.getType());
            return ResponseEntity.ok(response);
        } catch (EntityCreationException | EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/documents", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createCase(@PathVariable UUID caseUUID, @RequestBody AddDocumentsRequest request) {
        try {
            workflowService.addDocuments(caseUUID, request.getDocumentSummaries());
            return ResponseEntity.ok().build();
        } catch (EntityCreationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) throws EntityCreationException, EntityNotFoundException {
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> submitStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AddCaseDataRequest request) {
        try {
            GetStageResponse response = workflowService.updateCase(caseUUID, stageUUID, request.getData());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException | EntityCreationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}