package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkStageUserRequest;

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

    @Before
    public void setup() {
        caseworkClient = new CaseworkClient(restHelper, caseServiceUrl);
    }

    @Test
    public void updateStageUser(){
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();

        String expectedUrl = String.format("/case/%s/stage/%s/user", caseUUID, stageUUID);
        UpdateCaseworkStageUserRequest expectedBody = new UpdateCaseworkStageUserRequest(userUUID);

        caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(expectedBody), eq(Void.class));

        verifyNoMoreInteractions(restHelper);
    }
}
