package uk.gov.digital.ho.hocs.workflow.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowServiceTest {

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private DocumentClient documentClient;

    @Mock
    private InfoClient infoClient;

    private WorkflowService workflowService;

    private final String testFieldName = "field_name";
    private final UUID testDocumentUuid = UUID.randomUUID();

    @Before
    public void beforeTest(){
        workflowService = new WorkflowService(
                caseworkClient,
                documentClient,
                infoClient,
                camundaClient);
    }

    @Test
    public void getCreateCaseRequest_WhenDropdownTeams() {

        Map<String, Object> props = new HashMap<>();
        props.put("choices", "TEAMS");
        FieldDto fieldDto = mock(FieldDto.class);
        when(fieldDto.getComponent()).thenReturn("dropdown");
        when(fieldDto.getName()).thenReturn("uuid");
        when(fieldDto.getProps()).thenReturn(props);
        List<FieldDto> fieldDtos = new ArrayList<>();
        fieldDtos.add(fieldDto);
        SchemaDto schemaDto = mock(SchemaDto.class);
        when(schemaDto.getFields()).thenReturn(fieldDtos);
        Set<SchemaDto> schemaDtos = new HashSet<>();
        schemaDtos.add(schemaDto);
        // assign data map
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("uuid", "11111111-1111-1111-1111-111111111111");
        // assign info client
        TeamDto teamDto = mock(TeamDto.class);
        when(teamDto.getDisplayName()).thenReturn("teamName");
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        assertThat(uuid).isNotNull();
        when(infoClient.getTeam(uuid)).thenReturn(teamDto);

        Map<String, String> dataMapResult = workflowService.convertDataToSchema(schemaDtos, dataMap);

        assertThat(dataMapResult).isNotNull();
        assertThat(dataMapResult.size()).isEqualTo(1);
        assertThat(dataMapResult.get("uuid")).isEqualTo("teamName");

        verify(infoClient).getTeam(uuid);
        // assert nothing else happened
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);

    }

    @Test
    public void getCreateCaseRequest_WhenEntitylistDocuments() {

        Set<SchemaDto> schemaDtos = setupTestSchemas();

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(testFieldName, testDocumentUuid.toString());

        when(documentClient.getDocumentName(testDocumentUuid)).thenReturn("Document Name");

        Map<String, String> dataMapResult = workflowService.convertDataToSchema(schemaDtos, dataMap);

        assertThat(dataMapResult).isNotNull();
        assertThat(dataMapResult.size()).isOne();
        assertThat(dataMapResult.get(testFieldName)).isEqualTo("Document Name");
        verify(documentClient).getDocumentName(testDocumentUuid);

        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);

    }

    @Test
    public void convertDataToSchema_documentNameNotFoundException() {
        Set<SchemaDto> schemaDtos = setupTestSchemas();

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(testFieldName, testDocumentUuid.toString());

        when(documentClient.getDocumentName(testDocumentUuid)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));


        Map<String, String> dataMapResult = workflowService.convertDataToSchema(schemaDtos, dataMap);

        assertThat(dataMapResult).isNotNull();
        assertThat(dataMapResult.size()).isOne();
        assertThat(dataMapResult.get(testFieldName)).isEqualTo("Document not found");

        verify(documentClient).getDocumentName(testDocumentUuid);
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);

    }

    @Test(expected = HttpClientErrorException.class)
    public void convertDataToSchema_documentNameOtherException() {
        Set<SchemaDto> schemaDtos = setupTestSchemas();

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(testFieldName, testDocumentUuid.toString());

        when(documentClient.getDocumentName(testDocumentUuid)).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        Map<String, String> dataMapResult = workflowService.convertDataToSchema(schemaDtos, dataMap);

        assertThat(dataMapResult).isNotNull();
        assertThat(dataMapResult.size()).isOne();
        assertThat(dataMapResult.get(testFieldName)).isEqualTo("Document not found");

        verify(documentClient).getDocumentName(testDocumentUuid);
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);

    }

    @Test
    public void updateStage_forwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();
        Map<String, String> values = new HashMap<>();
        Direction direction = Direction.FORWARD;

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("valid", "true");
        expectedValues.put("DIRECTION", "FORWARD");

        workflowService.updateStage(caseUUID, stageUUID, values, direction);

        verify(caseworkClient).updateCase(caseUUID, stageUUID, expectedValues);
        verify(camundaClient).completeTask(stageUUID, expectedValues);

        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }

    @Test
    public void updateStage_backwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();
        Map<String, String> values = new HashMap<>();
        Direction direction = Direction.BACKWARD;

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("valid", "false");
        expectedValues.put("DIRECTION", "BACKWARD");

        workflowService.updateStage(caseUUID, stageUUID, values, direction);

        verify(camundaClient).completeTask(stageUUID, expectedValues);

        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }


    private Set<SchemaDto> setupTestSchemas() {

        Map<String, Object> props = Map.of("entity", "document");
        List<FieldDto> fieldDtos = List.of(new FieldDto(null, testFieldName, null, "entity-list", null, props, true, true));
        SchemaDto schemaDto = new SchemaDto(UUID.randomUUID(), null, null, null, null, true, fieldDtos, null);
        return Set.of(schemaDto);
    }
}
