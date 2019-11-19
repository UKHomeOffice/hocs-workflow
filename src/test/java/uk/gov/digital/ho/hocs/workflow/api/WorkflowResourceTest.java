package uk.gov.digital.ho.hocs.workflow.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.workflow.api.dto.AddCaseDataRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowResourceTest {

    @Mock
    private WorkflowService workflowService;

    private WorkflowResource workflowResource;

    @Before
    public void beforeTest(){
        workflowResource = new WorkflowResource(workflowService);
    }

    @Test
    public void updateStage_forwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();

        Map<String, String> data = new HashMap<>();
        data.put("dummyKey", "dummyValue");
        AddCaseDataRequest addCaseDataRequest = new AddCaseDataRequest(data);
        GetStageResponse mockStageResponse = mock(GetStageResponse.class);

        when(workflowService.getStage(caseUUID, stageUUID)).thenReturn(mockStageResponse);

        ResponseEntity<GetStageResponse> result = workflowResource.updateStageForward(caseUUID, stageUUID, addCaseDataRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(mockStageResponse);
        verify(workflowService).updateStage(caseUUID, stageUUID, data, Direction.FORWARD);
        verify(workflowService).getStage(caseUUID, stageUUID);
        verifyNoMoreInteractions(workflowService);

    }

    @Test
    public void updateStage_backwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();

        Map<String, String> data = new HashMap<>();
        data.put("dummyKey", "dummyValue");
        GetStageResponse mockStageResponse = mock(GetStageResponse.class);

        when(workflowService.getStage(caseUUID, stageUUID)).thenReturn(mockStageResponse);

        ResponseEntity<GetStageResponse> result = workflowResource.updateStageBackward(caseUUID, stageUUID);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(mockStageResponse);
        verify(workflowService).updateStage(caseUUID, stageUUID, new HashMap<>(), Direction.BACKWARD);
        verify(workflowService).getStage(caseUUID, stageUUID);
        verifyNoMoreInteractions(workflowService);

    }


}
