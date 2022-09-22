package uk.gov.digital.ho.hocs.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.security.AccessLevel;
import uk.gov.digital.ho.hocs.workflow.security.Allocated;
import uk.gov.digital.ho.hocs.workflow.security.AllocationLevel;
import uk.gov.digital.ho.hocs.workflow.security.Authorised;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
class WorkflowResource {

    private WorkflowService workflowService;

    private BpmnService bpmnService;

    @Autowired
    public WorkflowResource(WorkflowService workflowService, BpmnService bpmnService) {
        this.workflowService = workflowService;
        this.bpmnService = bpmnService;
    }

    @Authorised(accessLevel = AccessLevel.OWNER, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @PostMapping(value = "/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request,
                                                         @RequestHeader(RequestData.USER_ID_HEADER) UUID userUUID) {
        CreateCaseResponse response = workflowService.createCase(request.getType(), request.getDateReceived(),
            request.getDocuments(), userUUID, request.getFromCaseUUID(), request.getData());
        return ResponseEntity.ok(response);
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @PostMapping(value = "/case/bulk", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateBulkCaseResponse> createCaseBulk(@RequestBody CreateCaseRequest request,
                                                                 @RequestHeader(RequestData.USER_ID_HEADER)
                                                                 UUID userUUID) {
        List<DocumentSummary> list = request.getDocuments();
        list.forEach((documentSummary) -> workflowService.createCase(request.getType(), request.getDateReceived(),
            Collections.singletonList(documentSummary), userUUID, null, request.getData()));
        return ResponseEntity.ok(new CreateBulkCaseResponse(list.size()));
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> updateStageForward(@PathVariable UUID caseUUID,
                                                               @PathVariable UUID stageUUID,
                                                               @RequestBody AddCaseDataRequest request,
                                                               @RequestHeader(RequestData.USER_ID_HEADER)
                                                               UUID userUUID) {
        workflowService.updateStage(caseUUID, stageUUID, request.getData(), Direction.FORWARD.getValue(), userUUID);
        return ResponseEntity.ok(workflowService.getStage(caseUUID, stageUUID));
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/direction/{flowDirection}",
                 produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> updateStageWithDirection(@PathVariable UUID caseUUID,
                                                                     @PathVariable UUID stageUUID,
                                                                     @PathVariable String flowDirection,
                                                                     @RequestHeader(RequestData.USER_ID_HEADER)
                                                                     UUID userUUID) {
        workflowService.updateStage(caseUUID, stageUUID, new HashMap<>(), flowDirection, userUUID);
        return ResponseEntity.ok(workflowService.getStage(caseUUID, stageUUID));
    }

    @Allocated(allocatedTo = AllocationLevel.TEAM_USER)
    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(response);
    }

    @Authorised(accessLevel = AccessLevel.WRITE, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @PostMapping(value = "/case/{caseUUID}/document", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createDocument(@PathVariable UUID caseUUID, @RequestBody CreateDocumentRequest request) {
        workflowService.createDocument(caseUUID, request.getDocuments());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/{caseUUID}/action/{actionDataItemUuid}/document", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity createDocument(@PathVariable UUID caseUUID,
                                         @PathVariable UUID actionDataItemUuid,
                                         @RequestBody CreateDocumentRequest request) {
        workflowService.createDocument(caseUUID, actionDataItemUuid, request.getDocuments());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @GetMapping(value = "/case/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCaseResponse> getCase(@PathVariable UUID caseUUID) {
        GetCaseResponse response = workflowService.getAllCaseStages(caseUUID);
        return ResponseEntity.ok(response);
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/details/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCaseDetailsResponse> getReadOnlyCaseDetails(@PathVariable UUID caseUUID) {
        GetCaseDetailsResponse response = workflowService.getReadOnlyCaseDetails(caseUUID);
        return ResponseEntity.ok(response);
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/{variable}")
    public ResponseEntity updateCaseValue(@PathVariable UUID caseUUID,
                                          @PathVariable UUID stageUUID,
                                          @PathVariable String variable,
                                          @RequestBody UpdateCaseValueRequest request) {
        bpmnService.updateValue(caseUUID.toString(), stageUUID.toString(), variable, request.getValue());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/data")
    public ResponseEntity<Void> updateCaseDataValues(@PathVariable UUID caseUUID,
                                                     @PathVariable UUID stageUUID,
                                                     @RequestParam(name = "type", required = false) String type,
                                                     @RequestBody Map<String, String> request) {
        workflowService.updateCaseDataValues(caseUUID, stageUUID, type, request);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.CASE_ADMIN)
    @PutMapping(value = "/case/close/{caseUUID}")
    public ResponseEntity closeCase(@PathVariable UUID caseUUID) throws UnsupportedEncodingException {
        ResponseEntity response = workflowService.closeCase(caseUUID);
        return response;
    }

}
