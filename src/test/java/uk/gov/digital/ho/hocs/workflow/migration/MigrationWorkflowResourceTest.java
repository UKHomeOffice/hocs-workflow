package uk.gov.digital.ho.hocs.workflow.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.migration.api.MigrateCaseRequest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.CASE_REFERENCE;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.DATE_RECEIVED;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.LAST_UPDATED_BY_USER;
import static uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponseBuilder.aGetCaseworkCaseDataResponse;

@RunWith(MockitoJUnitRunner.class)
public class MigrationWorkflowResourceTest {

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private CaseworkClient caseworkClient;

    @Test
    public void CreatesCamundaProcessFromExistingCase() {

        UUID caseUUID = java.util.UUID.randomUUID();
        String caseType = "COMP";
        String caseRef = "COMP/123456/22";
        Map<String, String> data = Map.of("test", "test");
        LocalDate caseDeadline = LocalDate.now().plusDays(10);
        ZonedDateTime created = ZonedDateTime.now();
        LocalDate received = LocalDate.now().minusDays(10);
        UUID userUUID = UUID.randomUUID();

        when(caseworkClient.getCase(caseUUID)).thenReturn(
            aGetCaseworkCaseDataResponse().withCaseDeadline(caseDeadline).withCreated(created).withDateReceived(
                received).withType(caseType).withUuid(caseUUID).withReference(caseRef).withData(data).build());

        Map<String, String> expectedDataMap = new HashMap<>(data);
        expectedDataMap.put(DATE_RECEIVED, received.toString());
        expectedDataMap.put(LAST_UPDATED_BY_USER, userUUID.toString());
        expectedDataMap.put(CASE_REFERENCE, caseRef);
        doNothing().when(camundaClient).startCase(caseUUID, caseType, expectedDataMap);

        MigrationWorkflowResource migrationWorkflowResource = new MigrationWorkflowResource(camundaClient,
            caseworkClient);
        migrationWorkflowResource.createCase(new MigrateCaseRequest(caseUUID), userUUID);

        verify(caseworkClient, times(1)).getCase(caseUUID);
        verify(camundaClient, times(1)).startCase(caseUUID, caseType, expectedDataMap);

    }

}