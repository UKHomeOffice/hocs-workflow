package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.RecreateCaseworkStageRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkStageUserRequest;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CaseworkClientTest {

    private CaseworkClient caseworkClient;

    @Mock
    private RestHelper restHelper;

    private String caseServiceUrl = "http://localhost:8082";

    private UUID caseUUID = UUID.randomUUID();
    private UUID stageUUID = UUID.randomUUID();
    private UUID userUUID = UUID.randomUUID();
    private String stageType = "TypeA";

    @Before
    public void setup() {
        caseworkClient = new CaseworkClient(restHelper, caseServiceUrl);
    }

    @Test
    public void calculateTotals(){
        String expectedUrl = String.format("/case/%s/stage/%s/calculateTotals", caseUUID, stageUUID);

        caseworkClient.calculateTotals(caseUUID, stageUUID, "list");

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq("list"), eq(Map.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updateStageUser(){
        String expectedUrl = String.format("/case/%s/stage/%s/user", caseUUID, stageUUID);
        UpdateCaseworkStageUserRequest expectedBody = new UpdateCaseworkStageUserRequest(userUUID);

        caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(expectedBody), eq(Void.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void recreateStage(){
        String expectedUrl = String.format("/case/%s/stage/%s/recreate", caseUUID, stageUUID);
        RecreateCaseworkStageRequest expectedBody = new RecreateCaseworkStageRequest(stageUUID, stageType);

        caseworkClient.recreateStage(caseUUID, stageUUID, stageType);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(expectedBody), eq(Void.class));

        verifyNoMoreInteractions(restHelper);

    }
}
