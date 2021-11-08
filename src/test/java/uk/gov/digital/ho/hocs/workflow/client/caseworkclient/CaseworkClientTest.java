package uk.gov.digital.ho.hocs.workflow.client.caseworkclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


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
    public void calculateTotals() {
        String expectedUrl = String.format("/case/%s/stage/%s/calculateTotals", caseUUID, stageUUID);

        caseworkClient.calculateTotals(caseUUID, stageUUID, "list");

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq("list"), eq(Map.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updateDeadlineDays() {
        String expectedUrl = String.format("/case/%s/stage/%s/deadline", caseUUID, stageUUID);

        caseworkClient.updateDeadlineDays(caseUUID, stageUUID, 123);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(123), eq(Void.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updateStageDeadline() {
        String expectedUrl = String.format("/case/%s/stage/%s/stageDeadline", caseUUID, stageUUID);

        caseworkClient.updateStageDeadline(caseUUID, stageUUID, "TEST", 7);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), any(UpdateStageDeadlineRequest.class), eq(Void.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updateStageUser() {
        String expectedUrl = String.format("/case/%s/stage/%s/user", caseUUID, stageUUID);
        UpdateCaseworkStageUserRequest expectedBody = new UpdateCaseworkStageUserRequest(userUUID);

        caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(expectedBody), eq(Void.class));

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void recreateStage() {
        String expectedUrl = String.format("/case/%s/stage/%s/recreate", caseUUID, stageUUID);
        RecreateCaseworkStageRequest expectedBody = new RecreateCaseworkStageRequest(stageUUID, stageType);

        caseworkClient.recreateStage(caseUUID, stageUUID, stageType);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(expectedBody), eq(Void.class));

        verifyNoMoreInteractions(restHelper);

    }

    @Test
    public void updateTeamByStageAndTexts() {
        String[] texts = {"Text1", "Text2"};
        String expectedUrl = String.format("/case/%s/stage/%s/teamTexts", caseUUID, stageUUID);
        UpdateCaseworkTeamStageTextResponse response = new UpdateCaseworkTeamStageTextResponse();
        when(restHelper.put(eq(caseServiceUrl), eq(expectedUrl), any(UpdateCaseworkTeamStageTextRequest.class), eq(UpdateCaseworkTeamStageTextResponse.class)))
                .thenReturn(response);

        caseworkClient.updateTeamByStageAndTexts(caseUUID, stageUUID, stageType, "teamUUIDKey", "teamNameKey", texts);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), any(UpdateCaseworkTeamStageTextRequest.class), eq(UpdateCaseworkTeamStageTextResponse.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getStageUser() {
        UUID expectedResult = UUID.randomUUID();
        String expectedUrl = String.format("/case/%s/stage/%s/user", caseUUID, stageUUID);
        when(restHelper.get(eq(caseServiceUrl), eq(expectedUrl), eq(UUID.class))).thenReturn(expectedResult);

        UUID result = caseworkClient.getStageUser(caseUUID, stageUUID);

        assertThat(result).isEqualTo(result);
        verify(restHelper).get(eq(caseServiceUrl), eq(expectedUrl), eq(UUID.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getStageTeam() {
        UUID expectedResult = UUID.randomUUID();
        String expectedUrl = String.format("/case/%s/stage/%s/team", caseUUID, stageUUID);
        when(restHelper.get(eq(caseServiceUrl), eq(expectedUrl), eq(UUID.class))).thenReturn(expectedResult);

        UUID result = caseworkClient.getStageTeam(caseUUID, stageUUID);

        assertThat(result).isEqualTo(result);
        verify(restHelper).get(eq(caseServiceUrl), eq(expectedUrl), eq(UUID.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDataValue() {
        String variableName = "TestVariable";
        String expectedValue = "TestValue";
        String expectedUrl = String.format("/case/%s/data/%s", caseUUID, variableName);
        when(restHelper.get(eq(caseServiceUrl), eq(expectedUrl), eq(String.class))).thenReturn(expectedValue);

        String result = caseworkClient.getDataValue(caseUUID.toString(), variableName);

        assertThat(result).isEqualTo(result);
        verify(restHelper).get(eq(caseServiceUrl), eq(expectedUrl), eq(String.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updateDataValue() {
        String variableName = "TestVariable";
        String newValue = "TestValue";
        String expectedUrl = String.format("/case/%s/data/%s", caseUUID, variableName);

        caseworkClient.updateDataValue(caseUUID.toString(), variableName, newValue);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), eq(newValue), eq(Void.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getAllStagesForCase() {
        String resourcePath = String.format("/stage/case/%s", caseUUID);
        GetAllStagesForCaseResponse expectedResponse = new GetAllStagesForCaseResponse();

        when(
                restHelper.get(
                        eq(caseServiceUrl),
                        eq(resourcePath),
                        eq(GetAllStagesForCaseResponse.class))
        ).thenReturn(expectedResponse);

        GetAllStagesForCaseResponse result = caseworkClient.getAllStagesForCase(caseUUID);

        verify(restHelper).get(eq(caseServiceUrl), eq(resourcePath), eq(GetAllStagesForCaseResponse.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updateDeadlineForStages() {

        Map<String, Integer> stageTypeAndDaysMap = Map.of("a_stage_type", 10);

        String expectedUrl = String.format("/case/%s/stage/%s/stageDeadlines", caseUUID, stageUUID);

        caseworkClient.updateDeadlineForStages(caseUUID, stageUUID, stageTypeAndDaysMap);

        verify(restHelper).put(eq(caseServiceUrl), eq(expectedUrl), any(UpdateDeadlineForStagesRequest.class), eq(Void.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getActiveStageShouldReturnOneIfItExists() {

        String resourcePath = String.format("/stage/case/%s", caseUUID);
        StageDto stageDtoWithoutTeam = new StageDto(UUID.randomUUID(), "type", null);
        StageDto stageDtoWithTeam = new StageDto(UUID.randomUUID(), "type", UUID.randomUUID());
        List<StageDto> stages = new ArrayList<>();
        stages.add(stageDtoWithoutTeam);
        stages.add(stageDtoWithTeam);
        GetAllStagesForCaseResponse expectedResponse = new GetAllStagesForCaseResponse(stages);

        when(restHelper.get(eq(caseServiceUrl), eq(resourcePath), eq(GetAllStagesForCaseResponse.class))).thenReturn(expectedResponse);

        Optional<StageDto> optionalStageDto = caseworkClient.getActiveStage(caseUUID);
        assertThat(optionalStageDto.isPresent()).isTrue();
        StageDto responseStageDto = optionalStageDto.get();
        assertThat(responseStageDto).isEqualTo(stageDtoWithTeam);
        verify(restHelper).get(eq(caseServiceUrl), eq(resourcePath), eq(GetAllStagesForCaseResponse.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getActiveStageShouldReturnEmptyOptionalIfNoneExists() {

        String resourcePath = String.format("/stage/case/%s", caseUUID);
        StageDto stageDtoWithoutTeam = new StageDto(UUID.randomUUID(), "type", null);
        StageDto stageDtoWithoutTeam2 = new StageDto(UUID.randomUUID(), "type", null);
        List<StageDto> stages = new ArrayList<>();
        stages.add(stageDtoWithoutTeam);
        stages.add(stageDtoWithoutTeam2);
        GetAllStagesForCaseResponse expectedResponse = new GetAllStagesForCaseResponse(stages);

        when(restHelper.get(eq(caseServiceUrl), eq(resourcePath), eq(GetAllStagesForCaseResponse.class))).thenReturn(expectedResponse);

        Optional<StageDto> optionalStageDto = caseworkClient.getActiveStage(caseUUID);
        assertThat(optionalStageDto.isEmpty());
        verify(restHelper).get(eq(caseServiceUrl), eq(resourcePath), eq(GetAllStagesForCaseResponse.class));
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void updatePrimaryTopicForCase(){
        // GIVEN
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID topicUUID = UUID.randomUUID();
        String resourcePath = String.format("/case/%s/stage/%s/primaryTopic", caseUUID, stageUUID);

        // WHEN
        caseworkClient.updatePrimaryTopic(caseUUID, stageUUID, topicUUID);

        // THEN
        verify(restHelper, times(1)).put(eq(caseServiceUrl), eq(resourcePath), any(), any());
        verifyNoMoreInteractions(restHelper);
    }
}
