package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoGetStandardLineListResponse;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoGetTemplateListResponse;
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

    @PostMapping(value = "/case/{caseUUID}/document", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createDocument(@PathVariable UUID caseUUID,@RequestBody CreateDocumentRequest request) {
        workflowService.createDocument(caseUUID, request.getDocuments());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/case/{caseUUID}/document/{documentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteDocument(@PathVariable UUID caseUUID, @PathVariable UUID documentUUID) {
        workflowService.deleteDocument(caseUUID, documentUUID);
        return ResponseEntity.ok().build();
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

    @GetMapping(value = "/case/{caseUUID}/correspondent/{correspondentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCorrespondentResponse> getCorrespondentForCase(@PathVariable UUID caseUUID, @PathVariable UUID correspondentUUID) {
        GetCorrespondentResponse response = workflowService.getCorrespondentData(caseUUID, correspondentUUID);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/case/{caseUUID}/correspondent", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addCorrespondentToCase(@PathVariable UUID caseUUID, @RequestBody AddCorrespondentRequest request) {
        workflowService.addCorrespondentToCase(caseUUID, request.getType(), request.getFullname(), request.getPostcode(), request.getAddress1(), request.getAddress2(), request.getAddress3(), request.getCountry(),request.getEmail(), request.getTelephone(), request.getReference());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/case/{caseUUID}/correspondent/{correspondentUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteCorrespondentFromCase(@PathVariable UUID caseUUID, @PathVariable UUID correspondentUUID) {
        workflowService.deleteCorrespondentFromCase(caseUUID, correspondentUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/topiclist", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetParentTopicResponse> getParentTopicsAndTopics(@PathVariable UUID caseUUID) {
        GetParentTopicResponse response = workflowService.getParentTopicsAndTopics(caseUUID);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/case/{caseUUID}/topic", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCaseTopicsResponse> getCaseTopics(@PathVariable UUID caseUUID) {
        GetCaseTopicsResponse response = workflowService.getCaseTopics(caseUUID);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/case/{caseUUID}/topic/{topicUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getTopicData(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        Topic response = workflowService.getTopicData(topicUUID);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/case/{caseUUID}/topic", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addTopicToCase(@PathVariable UUID caseUUID, @RequestBody AddTopicRequest request) {
        workflowService.addTopicToCase(caseUUID, request.getTopicUUID());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/case/{caseUUID}/topic/{topicUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteTopicFromCase(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        workflowService.deleteTopicFromCase(caseUUID, topicUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/templates", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<InfoGetTemplateListResponse> getTemplateList(@PathVariable UUID caseUUID) {
        InfoGetTemplateListResponse response = workflowService.getTemplatesList(caseUUID);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/case/{caseUUID}/standard_lines", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<InfoGetStandardLineListResponse> getStandardLinesList(@PathVariable UUID caseUUID) {
        InfoGetStandardLineListResponse response = workflowService.getStandardLineList(caseUUID);
        return ResponseEntity.ok(response);
    }
}