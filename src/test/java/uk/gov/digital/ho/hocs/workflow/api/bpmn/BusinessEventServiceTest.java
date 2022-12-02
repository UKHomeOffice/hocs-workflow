package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.api.WorkflowService;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.BusinessEventPayloadInterface;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.DataFieldUpdatedPayload;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Field;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BusinessEventServiceTest {
    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private BpmnService bpmnService;

    @Mock
    private WorkflowService workflowService;

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
        businessEventService = new BusinessEventService(auditClient, caseworkClient, bpmnService, workflowService);
    }

    @Test
    public void testShouldCreateComplaintType(){
        Map<String, String> data = new HashMap<>();
        data.put("previousCompType", null);

        GetStageResponse getStageResponse = createGetStageResponse();

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(workflowService.getStage(caseUUID, stageUUID)).thenReturn(getStageResponse);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), "CompType", complaintType);
    }

    @Test
    public void testShouldUpdateComplaintTypeToService(){

        Map<String, String> data = new HashMap<>();
        data.put("previousCompType", previousComplaintType);

        GetStageResponse getStageResponse = createGetStageResponse();

        BusinessEventPayloadInterface expectedPayload = new DataFieldUpdatedPayload(
            "CompType_UPDATED",
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
        when(workflowService.getStage(caseUUID, stageUUID)).thenReturn(getStageResponse);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), fieldName, complaintType);

        verify(auditClient).createBusinessEvent(eq(caseUUID), eq(stageUUID), eq(expectedPayload));
    }

    @NotNull
    private GetStageResponse createGetStageResponse() {
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
        HocsSchema hocsSchema = new HocsSchema(
            null,
            null,
            hocsFormFields,
            null,
            null,
            null,
            null
        );
        HocsForm hocsForm = new HocsForm(hocsSchema, Map.of());
        GetStageResponse getStageResponse = new GetStageResponse(stageUUID, "caseRef", hocsForm);
        return getStageResponse;
    }

    @Test
    public void testShouldNotUpdateComplaintTypeNoChange(){
        Map<String, String> data = new HashMap<>();
        String complaintType = "Service";
        data.put("previousCompType", complaintType);

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
            caseUUID, null, null, null,
            data, null, null, null, null,
            null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), "CompType", complaintType);

        verify(auditClient, never()).createBusinessEvent(any(), any(), any());
    }
}
