package uk.gov.digital.ho.hocs.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseTypeDetails;
import uk.gov.digital.ho.hocs.workflow.model.Form;

import java.util.List;
import java.util.UUID;


import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;


@RestController
class WorkflowResource {

    private WorkflowService workflowService;

    @Autowired
    public WorkflowResource(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }


    @RequestMapping(value = "/workflow", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CaseTypeDetails>> getAllWorkflowTypes() {
        List<CaseTypeDetails> workflowTypes = workflowService.getAllWorkflowTypes();
        if(workflowTypes != null && !workflowTypes.isEmpty()) {
            return ResponseEntity.ok(workflowTypes);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateWorkflowCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        try {
            CreateWorkflowCaseResponse response = workflowService.createNewCase(request.getCaseType());
            return ResponseEntity.ok(response);
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/document", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateWorkflowCaseResponse> createCase(@RequestBody AddDocumentRequest request) {
        try {
            workflowService.addDocument(request.documents);
            return ResponseEntity.ok().build();
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/start", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StartStageResponse> startStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        try {
            StartStageResponse response = workflowService.startStage(caseUUID, stageUUID);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/{stageUUID}/submit", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Form> submitStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AddCaseDataRequest request) {
        try {
            String screenName = workflowService.updateCase(caseUUID, stageUUID);
            return ResponseEntity.ok(new Form(screenName, null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}