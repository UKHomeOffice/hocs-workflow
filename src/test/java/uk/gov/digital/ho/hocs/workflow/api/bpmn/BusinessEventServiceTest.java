package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.api.FormService;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.BusinessEventPayloadInterface;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.DataFieldUpdatedPayload;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class BusinessEventServiceTest {
    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private BpmnService bpmnService;

    @Mock
    private FormService formService;

    @Autowired
    private ObjectMapper objectMapper;

    private BusinessEventService businessEventService;
    private final UUID caseUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    private final String previousComplaintType = "MinorMisconduct";
    private final String newValueLabel = "Service";
    private final String complaintType = "Service";
    private final String fieldName = "CompType";
    private final String fieldNameLabel = "Complaint type";
    private final String previousValueLabel = "Minor misconduct";

    @Before
    public void setup() {
        businessEventService = new BusinessEventService(auditClient, caseworkClient, bpmnService, formService, objectMapper);
    }

    @Test
    public void testShouldCreateComplaintType(){
        Map<String, String> data = new HashMap<>();
        String screenName = "screenName";
        data.put("XPreviousCompType", null);

        HocsSchema hocsSchema = createHocsSchema();

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(formService.getFormSchema(screenName)).thenReturn(hocsSchema);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), screenName, "CompType", complaintType);
    }

    @Test
    public void testShouldUpdateComplaintTypeToService(){
        Map<String, String> data = new HashMap<>();
        String screenName = "screenName";
        data.put("XPreviousCompType", previousComplaintType);

        HocsSchema hocsSchema = createHocsSchema();

        BusinessEventPayloadInterface expectedPayload = new DataFieldUpdatedPayload(
            "CASE_DATA_ITEM_UPDATED",
            fieldName,
            complaintType,
            previousComplaintType,
            fieldNameLabel,
            newValueLabel,
            previousValueLabel
        );

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(formService.getFormSchema(screenName)).thenReturn(hocsSchema);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), screenName, fieldName, complaintType);

        verify(auditClient).createBusinessEvent(eq(caseUUID), eq(stageUUID), eq(expectedPayload));
    }

    @Test
    public void testShouldUpdateComplaintTypeWithConditionalChoices(){
        Map<String, String> data = new HashMap<>();
        String screenName = "screenName";
        data.put("XPreviousCompType", previousComplaintType);
        data.put("name", "matches");

        HocsSchema hocsSchema = createHocsSchemaWithConditionalChoices();

        BusinessEventPayloadInterface expectedPayload = new DataFieldUpdatedPayload(
            "CASE_DATA_ITEM_UPDATED",
            fieldName,
            complaintType,
            previousComplaintType,
            fieldNameLabel,
            newValueLabel + "2",
            previousValueLabel + "2"
        );

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
            caseUUID, null, null, null,
            data, null, null, null, null,
            null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(formService.getFormSchema(screenName)).thenReturn(hocsSchema);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), screenName, fieldName, complaintType);

        verify(auditClient).createBusinessEvent(eq(caseUUID), eq(stageUUID), eq(expectedPayload));
    }

    @NotNull
    private HocsSchema createHocsSchema() {
        Map<String, Object> props = Map.of(
            "choices", List.of(
                Map.of("value", complaintType, "label", newValueLabel),
                Map.of("value", previousComplaintType, "label", previousValueLabel))
        );
        Field field = new Field(
            fieldName,
            fieldNameLabel,
            null,
            null,
            new HashMap<>(props),
            null
        );
        List<HocsFormField> hocsFormFields = List.of(HocsFormField.from(field));
        return new HocsSchema(
            null,
            null,
            hocsFormFields,
            null,
            null,
            null,
            null
        );
    }

    @NotNull
    private HocsSchema createHocsSchemaWithConditionalChoices() {
        Map<String, Object> props = Map.of(
            "conditionChoices", List.of(
                Map.of(
                    "choices", List.of(
                        Map.of("value", complaintType, "label", newValueLabel + "1"),
                        Map.of("value", previousComplaintType, "label", previousValueLabel+ "1")),
                    "conditionPropertyName", "name",
                    "conditionPropertyValue", "doesnt match"
                      ),
                Map.of(
                    "choices", List.of(
                        Map.of("value", complaintType, "label", newValueLabel + "2"),
                        Map.of("value", previousComplaintType, "label", previousValueLabel + "2")),
                    "conditionPropertyName", "name",
                    "conditionPropertyValue", "matches"
                      )));
        Field field = new Field(
            fieldName,
            fieldNameLabel,
            null,
            null,
            new HashMap<>(props),
            null
        );
        List<HocsFormField> hocsFormFields = List.of(HocsFormField.from(field));
        return new HocsSchema(
            null,
            null,
            hocsFormFields,
            null,
            null,
            null,
            null
        );
    }

    @Test
    public void testShouldNotUpdateComplaintTypeNoChange(){
        Map<String, String> data = new HashMap<>();
        String complaintType = "Service";
        String screenName = "screenName";
        data.put("XPreviousCompType", complaintType);

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
            caseUUID, null, null, null,
            data, null, null, null, null,
            null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), screenName, "CompType", complaintType);

        verify(auditClient, never()).createBusinessEvent(any(), any(), any());
    }
}
