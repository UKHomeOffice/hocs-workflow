package uk.gov.digital.ho.hocs.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CreateWorkflowCaseResponse> createCase(@RequestBody CreateCaseRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            CreateWorkflowCaseResponse response = workflowService.createNewCase(request.getCaseType(), username);
            return ResponseEntity.ok(response);
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/document", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateWorkflowCaseResponse> createCase(@RequestBody AddDocumentRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            workflowService.addDocument(request.documents);
            return ResponseEntity.ok().build();
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/{stageUUID}/submit", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Form> endTask(@PathVariable UUID caseUUID,@PathVariable UUID stageUUID, @RequestBody AddCaseDataRequest request) {
        try {
            String screenName = workflowService.updateCase(caseUUID, stageUUID);
            return ResponseEntity.ok(new Form(screenName, null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}