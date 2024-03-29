package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.api.FormService;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.DataFieldUpdatedPayload;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormFieldChoice;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormFieldConditionChoice;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;

import java.util.List;
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
    private final FormService formService;
    private final ObjectMapper objectMapper;

    public BusinessEventService(AuditClient auditClient,
                                CaseworkClient caseworkClient,
                                BpmnService bpmnService,
                                FormService formService,
                                ObjectMapper objectMapper
                               ) {
        this.auditClient = auditClient;
        this.caseworkClient = caseworkClient;
        this.bpmnService = bpmnService;
        this.formService = formService;
        this.objectMapper = objectMapper;
    }

    public void createDataFieldUpdatedEvent(String caseUUIDString, String stageUUIDString, String screenName, String fieldName, String newValue) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID= UUID.fromString(stageUUIDString);

        String previousValueKey = String.format(PREVIOUS_VALUE_KEY, fieldName);

        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(caseUUID);
        String previousValue = caseData.getData().get(previousValueKey);

        if (Objects.equals(previousValue, newValue)) {
            return;
        }

        HocsSchema hocsSchema = formService.getFormSchema(screenName);

        List<HocsFormField> fields = hocsSchema.getFields();
        Optional<HocsFormField> hocsFormField = fields.stream().filter((field) -> Objects.equals(field.getProps().get("name"), fieldName)).findFirst();

        String fieldNameLabel = getFormProperty("label", hocsFormField, new TypeReference<String>() {}).orElse(fieldName);

        List<HocsFormFieldChoice> choices = getChoices(hocsFormField, caseData);

        String newValueLabel = getLabel(newValue, choices);
        String previousValueLabel = getLabel(previousValue, choices);


        DataFieldUpdatedPayload dataFieldUpdatedPayload = new DataFieldUpdatedPayload(
            DATA_FIELD_UPDATED_EVENT_NAME,
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

    private List<HocsFormFieldChoice> getChoices(Optional<HocsFormField> hocsFormField, GetCaseworkCaseDataResponse caseData) {
        List<HocsFormFieldConditionChoice> conditionChoices = getFormProperty("conditionChoices", hocsFormField,
            new TypeReference<List<HocsFormFieldConditionChoice>>() {}).orElse(List.of());

        for (HocsFormFieldConditionChoice choice : conditionChoices) {
            String caseDataValue = caseData.getData().get(choice.getConditionPropertyName());
            if (Objects.equals(choice.getConditionPropertyValue(), caseDataValue)) {
                return choice.getChoices();
            }
        }

        return getFormProperty("choices", hocsFormField, new TypeReference<List<HocsFormFieldChoice>>() {}).orElse(
            List.of());
    }

    private String getLabel(String value, List<HocsFormFieldChoice> choices) {
        return choices.stream()
            .filter(choice -> Objects.equals(choice.getValue(), value))
            .findFirst()
            .map(HocsFormFieldChoice::getLabel)
            .orElse(value);
    }

    private <T> Optional<T> getFormProperty(String name, Optional<HocsFormField> hocsFormField, TypeReference<T> typeRef) {
        return hocsFormField
            .flatMap((field) -> Optional.ofNullable(field.getProps().get(name)))
            .map(obj -> objectMapper.convertValue(obj, typeRef));
    }

}
