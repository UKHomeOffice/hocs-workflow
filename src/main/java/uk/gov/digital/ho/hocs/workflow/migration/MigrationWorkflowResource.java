package uk.gov.digital.ho.hocs.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.migration.api.MigrateCaseRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@Slf4j
public class MigrationWorkflowResource {

    private final CamundaClient camundaClient;

    private final CaseworkClient caseworkClient;

    private final AuditClient auditClient;

    public MigrationWorkflowResource(CamundaClient camundaClient, CaseworkClient caseworkClient, AuditClient auditClient) {
        this.camundaClient = camundaClient;
        this.caseworkClient = caseworkClient;
        this.auditClient = auditClient;
    }

    @PostMapping(value = "/migrate/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Void> createCase(@RequestBody MigrateCaseRequest request,
                                     @RequestHeader(RequestData.USER_ID_HEADER) UUID userUUID) {

        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(request.getCaseUUID());

        Map<String, String> seedData = new HashMap<>(caseData.getData());
        seedData.put(WorkflowConstants.DATE_RECEIVED, caseData.getDateReceived().toString());
        seedData.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());
        seedData.put(WorkflowConstants.CASE_REFERENCE, caseData.getReference());

        camundaClient.startCase(request.getCaseUUID(), caseData.getType(), seedData);

        auditClient.reopenCase(request.getCaseUUID());

        return ResponseEntity.ok().build();
    }


}
