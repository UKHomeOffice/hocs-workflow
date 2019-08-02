package uk.gov.digital.ho.hocs.workflow.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.mockito.Mockito.verifyZeroInteractions;

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

    @Test
    public void getCreateCaseRequest() {

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
        // assign service
        WorkflowService workflowService = new WorkflowService(
                caseworkClient,
                documentClient,
                infoClient,
                camundaClient);

        Map<String, String> dataMapResult = workflowService.convertDataToSchema(schemaDtos, dataMap);

        assertThat(dataMapResult).isNotNull();
        assertThat(dataMapResult.size()).isEqualTo(1);
        assertThat(dataMapResult.get("uuid")).isEqualTo("teamName");
        // assert nothing else happened
        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(documentClient);
    }
}
