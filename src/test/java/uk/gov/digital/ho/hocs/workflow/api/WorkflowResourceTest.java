package uk.gov.digital.ho.hocs.workflow.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
    public void beforeTest() {
        workflowResource = new WorkflowResource(workflowService);
    }

    @Test
    public void updateStage_forwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();
        UUID userUUID = java.util.UUID.randomUUID();

        Map<String, String> data = new HashMap<>();
        data.put("dummyKey", "dummyValue");
        AddCaseDataRequest addCaseDataRequest = new AddCaseDataRequest(data);
        GetStageResponse mockStageResponse = mock(GetStageResponse.class);

        when(workflowService.getStage(caseUUID, stageUUID)).thenReturn(mockStageResponse);

        ResponseEntity<GetStageResponse> result = workflowResource.updateStageForward(caseUUID, stageUUID, addCaseDataRequest, userUUID);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(mockStageResponse);
        verify(workflowService).updateStage(caseUUID, stageUUID, data, Direction.FORWARD, userUUID);
        verify(workflowService).getStage(caseUUID, stageUUID);
        verifyNoMoreInteractions(workflowService);

    }

    @Test
    public void updateStage_backwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();
        UUID userUUID = java.util.UUID.randomUUID();

        Map<String, String> data = new HashMap<>();
        data.put("dummyKey", "dummyValue");
        GetStageResponse mockStageResponse = mock(GetStageResponse.class);

        when(workflowService.getStage(caseUUID, stageUUID)).thenReturn(mockStageResponse);

        ResponseEntity<GetStageResponse> result = workflowResource.updateStageBackward(caseUUID, stageUUID, userUUID);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(mockStageResponse);
        verify(workflowService).updateStage(caseUUID, stageUUID, new HashMap<>(), Direction.BACKWARD, userUUID);
        verify(workflowService).getStage(caseUUID, stageUUID);
        verifyNoMoreInteractions(workflowService);

    }

    @Test
    public void createCase() {

        UUID caseUUID = UUID.randomUUID();
        String caseRef = "caseRefABC";
        UUID userUUID = UUID.randomUUID();
        String caseType = "type1";
        LocalDate dateReceived = LocalDate.now();
        List<DocumentSummary> documents = List.of(new DocumentSummary("Doc1.txt", "FINAL", "locationUrl"));
        CreateCaseRequest request = new CreateCaseRequest(caseType, dateReceived, documents);
        CreateCaseResponse response = new CreateCaseResponse(caseUUID, caseRef);

        when(workflowService.createCase(caseType, dateReceived, documents, userUUID)).thenReturn(response);

        ResponseEntity<CreateCaseResponse> result = workflowResource.createCase(request, userUUID);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getUuid()).isEqualTo(caseUUID);
        assertThat(result.getBody().getReference()).isEqualTo(caseRef);

        verify(workflowService).createCase(caseType, dateReceived, documents, userUUID);
        verifyNoMoreInteractions(workflowService);

    }


    @Test
    public void createBulkCase() {

        UUID caseUUID = UUID.randomUUID();
        String caseRef = "caseRefABC";
        UUID userUUID = UUID.randomUUID();
        String caseType = "type1";
        LocalDate dateReceived = LocalDate.now();
        DocumentSummary doc1 = new DocumentSummary("Doc1.txt", "FINAL", "locationUrl");
        DocumentSummary doc2 = new DocumentSummary("Doc2.txt", "DRAFT", "locationUrl2");
        List<DocumentSummary> documents = List.of(doc1, doc2);
        CreateCaseRequest request = new CreateCaseRequest(caseType, dateReceived, documents);
        CreateCaseResponse response = new CreateCaseResponse(caseUUID, caseRef);

        when(workflowService.createCase(caseType, dateReceived, List.of(doc1), userUUID)).thenReturn(response);
        when(workflowService.createCase(caseType, dateReceived, List.of(doc2), userUUID)).thenReturn(response);

        ResponseEntity<CreateBulkCaseResponse> result = workflowResource.createCaseBulk(request, userUUID);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCount()).isEqualTo(2);

        verify(workflowService).createCase(caseType, dateReceived, List.of(doc1), userUUID);
        verify(workflowService).createCase(caseType, dateReceived, List.of(doc2), userUUID);
        verifyNoMoreInteractions(workflowService);

    }


}
