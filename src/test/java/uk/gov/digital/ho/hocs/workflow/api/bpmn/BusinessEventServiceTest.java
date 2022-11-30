package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.BusinessEventPayloadInterface;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.DataFieldUpdatedPayload;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
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

    private BusinessEventService businessEventService;
    private final UUID caseUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    @Before
    public void setup() {
        businessEventService = new BusinessEventService(auditClient, caseworkClient, bpmnService);
    }

    @Test
    public void testShouldCreateComplaintType(){
        Map<String, String> data = new HashMap<>();
        data.put("previousCompType", null);
        String complaintType = "Service";

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), "CompType", complaintType);
    }

    @Test
    public void testShouldUpdateComplaintTypeToService(){
        Map<String, String> data = new HashMap<>();
        String previousComplaintType = "MinorMisconduct";
        data.put("previousCompType", previousComplaintType);
        String complaintType = "Service";

        BusinessEventPayloadInterface expectedPayload = new DataFieldUpdatedPayload(
            "CompType_UPDATED",
            "CompType",
            complaintType,
            previousComplaintType
        );

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);

        businessEventService.createDataFieldUpdatedEvent(caseUUID.toString(), stageUUID.toString(), "CompType", complaintType);

        verify(auditClient).createBusinessEvent(eq(caseUUID), eq(stageUUID), eq(expectedPayload));
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
