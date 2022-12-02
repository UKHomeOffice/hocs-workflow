package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.api.WorkflowService;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.DataFieldUpdatedPayload;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.DATA_FIELD_UPDATED_EVENT_NAME;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.PREVIOUS_VALUE_KEY;

@Service
@Slf4j
public class BusinessEventService {
    private final AuditClient auditClient;
    private final CaseworkClient caseworkClient;
    private final BpmnService bpmnService;
    private final WorkflowService workflowService;

    public BusinessEventService(AuditClient auditClient,
                                CaseworkClient caseworkClient,
                                BpmnService bpmnService,
                                WorkflowService workflowService) {
        this.auditClient = auditClient;
        this.caseworkClient = caseworkClient;
        this.bpmnService = bpmnService;
        this.workflowService = workflowService;
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

        GetStageResponse getStageResponse = workflowService.getStage(caseUUID, stageUUID);

        List<HocsFormField> fields = getStageResponse.getForm().getSchema().getFields();
        Optional<HocsFormField> hocsFormField = fields.stream().filter((field) -> Objects.equals(field.getProps().get("name"), fieldName)).findFirst();

        String fieldNameLabel = getFormProperty("label", hocsFormField, fieldName, String.class);

        @SuppressWarnings("unchecked") List<Map<String, String>> choices = getFormProperty("choices", hocsFormField, List.of(), List.class);

        String newValueLabel = getLabel(newValue, choices);
        String previousValueLabel = getLabel(previousValue, choices);


        DataFieldUpdatedPayload dataFieldUpdatedPayload = new DataFieldUpdatedPayload(
            name,
            fieldName,
            newValue,
            previousValue,
            fieldNameLabel,
            newValueLabel,
            previousValueLabel
        );

        auditClient.createBusinessEvent(caseUUID, stageUUID, dataFieldUpdatedPayload);
        bpmnService.updateCaseValue(caseUUIDString, stageUUIDString, previousValueKey, newValue);
    }

    private String getLabel(String value, List<Map<String, String>> choices) {
        return choices.stream()
            .filter(choice -> Objects.equals(choice.get("value"), value))
            .findFirst()
            .map(choice -> choice.get("label"))
            .orElse(value);
    }

    private <T> T getFormProperty(String name, Optional<HocsFormField> hocsFormField, T defaultValue, Class<T> classTag) {
        return hocsFormField
            .flatMap((field) -> Optional.ofNullable(field.getProps().get(name)))
            .map(classTag::cast)
            .orElse(defaultValue);
    }

}
