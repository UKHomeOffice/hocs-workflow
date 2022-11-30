package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.DataFieldUpdatedPayload;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

import java.util.Objects;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.DATA_FIELD_UPDATED_EVENT_NAME;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.PREVIOUS_VALUE_KEY;

@Service
@Slf4j
public class BusinessEventService {
    private final AuditClient auditClient;
    private final CaseworkClient caseworkClient;
    private final BpmnService bpmnService;

    public BusinessEventService(AuditClient auditClient, CaseworkClient caseworkClient, BpmnService bpmnService) {
        this.auditClient = auditClient;
        this.caseworkClient = caseworkClient;
        this.bpmnService = bpmnService;
    }

    public void createDataFieldUpdatedEvent(String caseUUIDString, String stageUUIDString, String fieldName, String newValue) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID= UUID.fromString(stageUUIDString);

        String previousValueKey = String.format(PREVIOUS_VALUE_KEY, fieldName);
        String name = String.format(DATA_FIELD_UPDATED_EVENT_NAME, fieldName);

        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(caseUUID);
        String previousValue = caseData.getData().get(previousValueKey);

        if (Objects.equals(previousValue, newValue)) {
            return;
        }

        DataFieldUpdatedPayload dataFieldUpdatedPayload = new DataFieldUpdatedPayload(
            name,
            fieldName,
            newValue,
            previousValue
        );

        auditClient.createBusinessEvent(caseUUID, stageUUID, dataFieldUpdatedPayload);
        bpmnService.updateCaseValue(caseUUIDString, stageUUIDString, previousValueKey, newValue);
    }

}
