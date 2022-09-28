package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BusinessEventServiceTest {
    @Mock
    private CaseworkClient caseworkClient;
    @Mock
    private AuditClient auditClient;

    private BpmnService bpmnService;
    private BusinessEventService businessEventService;
    private final UUID caseUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    @Before
    public void setup() {
        businessEventService = new BusinessEventService(auditClient, caseworkClient ,bpmnService);
    }

    @Test
    public void testShouldCreateCompliantType(){
        Map<String, String> data = new HashMap<>();
        String previousComplaintType = null;
        data.put("previousComplaintType", previousComplaintType);
        String complaintType = "Service";

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        businessEventService.createComplaintTypeEvent(caseUUID.toString(), stageUUID.toString(), complaintType);
    }

    /* @Test
    public void testShouldUpdateCompliantTypeToService(){
        Map<String, String> data = new HashMap<>();
        String previousComplaintType = "MinorMisconduct";
        data.put("previousComplaintType", previousComplaintType);
        String complaintType = "Service";

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(auditClient.createCaseComplaintType(caseUUID, stageUUID, complaintType, previousComplaintType)).thenReturn(complaintType);
        String updatedComplaintType = bpmnService.createComplaintType(caseUUID.toString(), stageUUID.toString(), complaintType);
        assertThat(complaintType.equalsIgnoreCase(updatedComplaintType));
    }

    @Test
    public void testShouldUpdateCompliantTypeToMinorMisconduct(){
        Map<String, String> data = new HashMap<>();
        String previousComplaintType = "Service";
        data.put("previousComplaintType", previousComplaintType);
        String complaintType = "MinorMisconduct";

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(auditClient.createCaseComplaintType(caseUUID, stageUUID, complaintType, previousComplaintType)).thenReturn(complaintType);
        String updatedComplaintType = bpmnService.createComplaintType(caseUUID.toString(), stageUUID.toString(), complaintType);
        assertThat(complaintType.equalsIgnoreCase(updatedComplaintType));
    }

    @Test
    public void testShouldUpdateCompliantTypeToSeriousMisconduct(){
        Map<String, String> data = new HashMap<>();
        String previousComplaintType = "Service";
        data.put("previousComplaintType", previousComplaintType);
        String complaintType = "SeriousMisconduct";

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null,
                data, null, null, null, null,
                null, null, false);

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(auditClient.createCaseComplaintType(caseUUID, stageUUID, complaintType, previousComplaintType)).thenReturn(complaintType);
        String updatedComplaintType = bpmnService.createComplaintType(caseUUID.toString(), stageUUID.toString(), complaintType);
        assertThat(complaintType.equalsIgnoreCase(updatedComplaintType));
    }*/
}
