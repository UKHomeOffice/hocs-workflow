package uk.gov.digital.ho.hocs.workflow.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDtoBuilder;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetCaseDetailsResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDtoBuilder;
import uk.gov.digital.ho.hocs.workflow.api.dto.SecondaryActionDto;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponseBuilder;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.StageDto;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.security.UserPermissionsService;

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

    @Mock
    private UserPermissionsService userPermissionsService;

    private WorkflowService workflowService;

    private final String testFieldName = "field_name";
    private final UUID testDocumentUuid = UUID.randomUUID();

    private final String schemaTitle = "schema-title";
    private final String fieldComponent = "f-comp";
    private final String fieldLabel = "f-label";
    private final String fieldName = "f-name";
    private final String schemaDefaultActionLabel = "schemaDefaultActionLabel";
    private final String secondaryActionComponent = "sa-component";
    private final String secondaryActionName = "sa-name";
    private final String secondaryActionLabel = "sa-label";
    private final String caseRef = "case-ref";
    private final Map<String, String> caseResponseData = new HashMap<>();
    private final Object[] fieldValidation = new Object[] {};
    private final String[] secondaryActionValidation = new String[] {};
    private final Object schemaDtoValidation = new Object();
    private final Object schemaDtoProps = new Object();

    @Before
    public void beforeTest() {
        workflowService = new WorkflowService(
                caseworkClient,
                documentClient,
                infoClient,
                camundaClient,
                userPermissionsService);
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
        List<SchemaDto> schemaDtos = new LinkedList<>();
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

        List<SchemaDto> schemaDtos = setupTestSchemas();

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
        List<SchemaDto> schemaDtos = setupTestSchemas();

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
        List<SchemaDto> schemaDtos = setupTestSchemas();

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
        UUID userUUID = java.util.UUID.randomUUID();
        Map<String, String> values = new HashMap<>();
        Direction direction = Direction.FORWARD;

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put(WorkflowConstants.VALID, "true");
        expectedValues.put(WorkflowConstants.DIRECTION, "FORWARD");
        expectedValues.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());

        workflowService.updateStage(caseUUID, stageUUID, values, direction.getValue(), userUUID);

        verify(caseworkClient).updateCase(caseUUID, stageUUID, expectedValues);
        verify(camundaClient).completeTask(stageUUID, expectedValues);

        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }

    @Test
    public void updateStage_backwardDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();
        UUID userUUID = java.util.UUID.randomUUID();
        Map<String, String> values = new HashMap<>();
        Direction direction = Direction.BACKWARD;


        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put(WorkflowConstants.VALID, "false");
        expectedValues.put(WorkflowConstants.DIRECTION, "BACKWARD");
        expectedValues.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());

        workflowService.updateStage(caseUUID, stageUUID, values, direction.getValue(), userUUID);

        verify(camundaClient).completeTask(stageUUID, expectedValues);

        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }

    @Test
    public void updateStage_customDirection() {

        UUID caseUUID = java.util.UUID.randomUUID();
        UUID stageUUID = java.util.UUID.randomUUID();
        UUID userUUID = java.util.UUID.randomUUID();
        Map<String, String> values = new HashMap<>();

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put(WorkflowConstants.VALID, "false");
        expectedValues.put(WorkflowConstants.DIRECTION, "testDirection");
        expectedValues.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());

        workflowService.updateStage(caseUUID, stageUUID, values, "testDirection", userUUID);

        verify(camundaClient).completeTask(stageUUID, expectedValues);

        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }

    @Test
    public void getReadOnlyCaseDetails() {
        UUID caseUUID = java.util.UUID.randomUUID();
        String caseType = "TypeC";
        String caseStages = "STAGE1,STAGE2,STAGE3";
        String caseRef = "Ref321";

        String nameA = "nameA";
        String componentA = "componentA";
        Map<String, Object> propertyA = new HashMap<>(Map.of("colour", "red"));
        String nameB = "nameB";
        String componentB = "componentB";
        Map<String, Object> propertyB = new HashMap<>(Map.of("colour", "blue"));

        Map<String, String> data = new HashMap<>();
        data.put(nameA, "value1");
        data.put(nameB, "value2");

        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = GetCaseworkCaseDataResponseBuilder.aGetCaseworkCaseDataResponse()
                .withUuid(caseUUID).withType(caseType).withData(data).withReference(caseRef).build();

        StageDto stageDto1 = new StageDto(UUID.randomUUID(), "STAGE1", UUID.randomUUID());
        StageDto stageDto2 = new StageDto(UUID.randomUUID(), "STAGE2", UUID.randomUUID());
        StageDto stageDto3 = new StageDto(UUID.randomUUID(), "STAGE3", UUID.randomUUID());
        List<StageDto> stageDtos = new ArrayList<StageDto>();
        stageDtos.add(stageDto1);
        stageDtos.add(stageDto2);
        stageDtos.add(stageDto3);
        GetAllStagesForCaseResponse allStagesForCaseResponse = new GetAllStagesForCaseResponse(stageDtos);

        CaseDetailsFieldDto caseDetailsFieldDtoA = new CaseDetailsFieldDto(nameA, componentA, propertyA);
        CaseDetailsFieldDto caseDetailsFieldDtoB = new CaseDetailsFieldDto(nameB, componentB, propertyB);

        FieldDto fieldDtoA = FieldDtoBuilder.aFieldDto().withName(nameA).withComponent(componentA).withProps(propertyA).build();
        FieldDto fieldDtoB = FieldDtoBuilder.aFieldDto().withName(nameB).withComponent(componentB).withProps(propertyB).build();

        SchemaDto schemaDto = SchemaDtoBuilder.aSchemaDto().withFields(List.of(fieldDtoA, fieldDtoB)).build();

        when(caseworkClient.getFullCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);
        when(caseworkClient.getAllStagesForCase(caseUUID)).thenReturn(allStagesForCaseResponse);
        when(infoClient.getCaseDetailsFieldsByCaseType(caseType)).thenReturn(List.of(caseDetailsFieldDtoA, caseDetailsFieldDtoB));
        when(infoClient.getSchemasForCaseTypeAndStages(caseType, caseStages)).thenReturn(List.of(schemaDto));

        GetCaseDetailsResponse result = workflowService.getReadOnlyCaseDetails(caseUUID);

        assertThat(result).isNotNull();
        assertThat(result.getSchema()).isNotNull();
        assertThat(result.getSchema().getTitle()).isEqualTo(caseRef);
        assertThat(result.getSchema().getFields()).isNotNull();
        assertThat(result.getSchema().getFields().size()).isEqualTo(2);
        assertThat(result.getSchema().getFields().get(0).getComponent()).isEqualTo(componentA);
        assertThat(result.getSchema().getFields().get(0).getProps().get("name")).isEqualTo(nameA);
        assertThat(result.getSchema().getFields().get(0).getProps().get("disabled")).isEqualTo(true);
        assertThat(result.getSchema().getFields().get(1).getComponent()).isEqualTo(componentB);
        assertThat(result.getSchema().getFields().get(1).getProps().get("name")).isEqualTo(nameB);
        assertThat(result.getSchema().getFields().get(1).getProps().get("disabled")).isEqualTo(true);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().size()).isEqualTo(2);
        assertThat(result.getData().get(nameA)).isEqualTo("value1");
        assertThat(result.getData().get(nameB)).isEqualTo("value2");

        verify(caseworkClient).getFullCase(caseUUID);
        verify(caseworkClient).getAllStagesForCase(caseUUID);
        verify(infoClient).getCaseDetailsFieldsByCaseType(caseType);
        verify(infoClient).getSchemasForCaseTypeAndStages(caseType, caseStages);
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }

    @Test
    public void getAllCaseStages() {
        UUID caseUUID = java.util.UUID.randomUUID();
        String caseType = "TypeC";
        String stageType1 = "STAGE_TYPE1";
        String stageType2 = "STAGE_TYPE2";
        String allCaseStages = "STAGE_TYPE1,STAGE_TYPE2";
        String caseRef = "View Case";

        String nameA = "nameA";
        String componentA = "componentA";
        Map<String, Object> propertyA = new HashMap<>(Map.of("colour", "red"));
        String nameB = "nameB";
        String componentB = "componentB";
        Map<String, Object> propertyB = new HashMap<>(Map.of("colour", "blue"));

        Map<String, String> data = new HashMap<>();
        data.put(nameA, "value1");
        data.put(nameB, "value2");

        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = GetCaseworkCaseDataResponseBuilder.aGetCaseworkCaseDataResponse()
                .withUuid(caseUUID).withType(caseType).withData(data).withReference(caseRef).build();

        StageDto stageDto1 = new StageDto(UUID.randomUUID(), stageType1, UUID.randomUUID());
        StageDto stageDto2 = new StageDto(UUID.randomUUID(), stageType2, UUID.randomUUID());
        List<StageDto> stageDtos = new ArrayList<StageDto>();
        stageDtos.add(stageDto1);
        stageDtos.add(stageDto2);
        GetAllStagesForCaseResponse allStagesForCaseResponse = new GetAllStagesForCaseResponse(stageDtos);

        FieldDto fieldDtoA = FieldDtoBuilder.aFieldDto().withName(nameA).withComponent(componentA).withProps(propertyA).build();
        FieldDto fieldDtoB = FieldDtoBuilder.aFieldDto().withName(nameB).withComponent(componentB).withProps(propertyB).build();
        SchemaDto schemaDto1 = SchemaDtoBuilder.aSchemaDto().withFields(List.of(fieldDtoA, fieldDtoB)).withStageType(stageType1).build();

        FieldDto fieldDto1 = FieldDtoBuilder.aFieldDto().withName(nameA).withComponent(componentA).withProps(propertyA).build();
        FieldDto fieldDto2 = FieldDtoBuilder.aFieldDto().withName(nameB).withComponent(componentB).withProps(propertyB).build();
        SchemaDto schemaDto2 = SchemaDtoBuilder.aSchemaDto().withFields(List.of(fieldDto1, fieldDto2)).withStageType(stageType2).build();

        List<SchemaDto> schemaDtos = new ArrayList<SchemaDto>();
        schemaDtos.add(schemaDto1);
        schemaDtos.add(schemaDto2);

        when(caseworkClient.getFullCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);
        when(caseworkClient.getAllStagesForCase(caseUUID)).thenReturn(allStagesForCaseResponse);
        when(infoClient.getSchemasForCaseTypeAndStages(caseType, allCaseStages)).thenReturn(schemaDtos);

        GetCaseResponse result = workflowService.getAllCaseStages(caseUUID);

        assertThat(result).isNotNull();
        assertThat(result.getSchema()).isNotNull();
        assertThat(result.getSchema().getTitle()).isEqualTo(caseRef);
        assertThat(result.getSchema().getFields()).isNotNull();
        assertThat(result.getSchema().getFields().size()).isEqualTo(2);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().size()).isEqualTo(2);
        assertThat(result.getData().get(nameA)).isEqualTo("value1");
        assertThat(result.getData().get(nameB)).isEqualTo("value2");

        verify(caseworkClient).getFullCase(caseUUID);
        verify(caseworkClient).getAllStagesForCase(caseUUID);
        verify(infoClient).getSchemasForCaseTypeAndStages(caseType, allCaseStages);
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient, documentClient);
    }

    @Test
    public void getStage() {
        //given
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String screenName = "DATA_INPUT";
        when(camundaClient.getStageScreenName(stageUUID)).thenReturn(screenName);
        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = new GetCaseworkCaseDataResponse(caseUUID, null, null, caseRef, caseResponseData, null, null, null, null, null, null);
        when(caseworkClient.getCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);
        SchemaDto schemaDto = exampleSchemaDto();
        when(infoClient.getSchema(screenName)).thenReturn(schemaDto);

        //when
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);

        //then
        assertThat(response.getStageUUID()).isSameAs(stageUUID);
        assertThat(response.getCaseReference()).isSameAs(caseRef);
        assertThat(response.getForm().getSchema().getTitle()).isSameAs(schemaTitle);
        assertThat(response.getForm().getSchema().getDefaultActionLabel()).isSameAs(schemaDefaultActionLabel);
        assertThat(response.getForm().getSchema().getFields().get(0).getComponent()).isSameAs(fieldComponent);
        assertThat(response.getForm().getSchema().getFields().get(0).getValidation()).isSameAs(fieldValidation);
        assertThat(response.getForm().getSchema().getFields().get(0).getProps().get("label")).isSameAs(fieldLabel);
        assertThat(response.getForm().getSchema().getFields().get(0).getProps().get("name")).isSameAs(fieldName);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getComponent()).isSameAs(secondaryActionComponent);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getValidation()).isSameAs(secondaryActionValidation);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getProps().get("label")).isSameAs(secondaryActionLabel);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getProps().get("name")).isSameAs(secondaryActionName);
        assertThat(response.getForm().getSchema().getProps()).isSameAs(schemaDtoProps);
        assertThat(response.getForm().getSchema().getValidation()).isSameAs(schemaDtoValidation);
        assertThat(response.getForm().getData()).isSameAs(caseResponseData);
    }

    @Test
    public void getStageReturnsWorkstackTriggeringResponseForFinishScreenNameWhenThereIsNoActiveStage() {
        //given
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String screenName = "FINISH";
        when(camundaClient.getStageScreenName(stageUUID)).thenReturn(screenName);
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.empty());

        //when
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);

        //then
        assertThat(response.getForm()).isNull();
        assertThat(response.getCaseReference()).isNull();
        assertThat(response.getStageUUID()).isEqualTo(stageUUID);
        verify(caseworkClient).getActiveStage(caseUUID);
        verifyNoMoreInteractions(caseworkClient, infoClient);
    }

    @Test
    public void getStageReturnsWorkstackTriggeringResponseForFinishScreenNameWhenNotOnNextStageTeam() {
        //given
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String screenName = "FINISH";
        when(camundaClient.getStageScreenName(stageUUID)).thenReturn(screenName);
        UUID nextStageUUID = UUID.randomUUID();
        StageDto nextStage = new StageDto(nextStageUUID, "stage-type", UUID.randomUUID());
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.of(nextStage));
        when(userPermissionsService.isUserOnTeam(nextStage.getTeamUUID())).thenReturn(false);
        when(camundaClient.hasProcessInstanceVariableWithValue(anyString(), anyString(), anyString())).thenReturn(true);

        //when
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);

        //then
        assertThat(response.getForm()).isNull();
        assertThat(response.getCaseReference()).isNull();
        assertThat(response.getStageUUID()).isEqualTo(stageUUID);
        verify(caseworkClient).getActiveStage(caseUUID);
        verifyNoMoreInteractions(caseworkClient, infoClient);
    }

    @Test
    public void getStageReturnsWorkstackTriggeringResponseForFinishScreenNameWhenStickyCasesOff() {
        //given
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String screenName = "FINISH";
        when(camundaClient.getStageScreenName(stageUUID)).thenReturn(screenName);
        UUID nextStageUUID = UUID.randomUUID();
        StageDto nextStage = new StageDto(nextStageUUID, "stage-type", UUID.randomUUID());
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.of(nextStage));
        when(userPermissionsService.isUserOnTeam(nextStage.getTeamUUID())).thenReturn(true);
        when(camundaClient.hasProcessInstanceVariableWithValue(anyString(), anyString(), anyString())).thenReturn(false);

        //when
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);

        //then
        assertThat(response.getForm()).isNull();
        assertThat(response.getCaseReference()).isNull();
        assertThat(response.getStageUUID()).isEqualTo(stageUUID);
        verify(caseworkClient).getActiveStage(caseUUID);
        verifyNoMoreInteractions(caseworkClient, infoClient);
    }

    @Test
    public void getStageReturnsDataForNextStageForFinishScreenNameWhenStickyCasesOnAndOnNextStageTeam() {
        //given
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        String screenName = "FINISH";
        when(camundaClient.getStageScreenName(stageUUID)).thenReturn(screenName);
        UUID nextStageUUID = UUID.randomUUID();
        String nextStageScreenName = "DATA-INPUT";
        when(camundaClient.getStageScreenName(nextStageUUID)).thenReturn(nextStageScreenName);
        StageDto nextStage = new StageDto(nextStageUUID, "stage-type", UUID.randomUUID());
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.of(nextStage));
        when(userPermissionsService.isUserOnTeam(nextStage.getTeamUUID())).thenReturn(true);
        when(userPermissionsService.getUserId()).thenReturn(userUUID);
        when(camundaClient.hasProcessInstanceVariableWithValue(caseUUID.toString(), "STICKY_CASES", "true")).thenReturn(true);
        when(infoClient.getSchema(nextStageScreenName)).thenReturn(exampleSchemaDto());
        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = new GetCaseworkCaseDataResponse(caseUUID, null, null, caseRef, caseResponseData, null, null, null, null, null, null);
        when(caseworkClient.getCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);

        //when
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);

        //then
        assertThat(response.getStageUUID()).isEqualTo(nextStageUUID);
        assertThat(response.getForm()).isNotNull();
        assertThat(response.getCaseReference()).isNotNull();
        verify(caseworkClient).updateStageUser(caseUUID, nextStageUUID, userUUID);
        verify(camundaClient).removeProcessInstanceVariableFromAllScopes(caseUUID.toString(), nextStageUUID.toString(), "STICKY_CASES");
    }

    private SchemaDto exampleSchemaDto() {
        FieldDto fieldDto = new FieldDto(UUID.randomUUID(), fieldName, fieldLabel, fieldComponent, fieldValidation, new HashMap<>(), false, false);
        List<FieldDto> fields = new ArrayList<>();
        fields.add(fieldDto);
        SecondaryActionDto secondaryActionDto = new SecondaryActionDto(UUID.randomUUID(), secondaryActionName, secondaryActionLabel, secondaryActionComponent, secondaryActionValidation, new HashMap<>());
        List<SecondaryActionDto> secondaryActions = new ArrayList<>();
        secondaryActions.add(secondaryActionDto);
        return new SchemaDto(UUID.randomUUID(), "schema-stage-type", "schema-type", schemaTitle, schemaDefaultActionLabel, false, fields, secondaryActions, schemaDtoProps, schemaDtoValidation);
    }

    private List<SchemaDto> setupTestSchemas() {

        Map<String, Object> props = Map.of("entity", "document");
        List<FieldDto> fieldDtos = List.of(new FieldDto(null, testFieldName, null, "entity-list", null, props, true, true));
        SchemaDto schemaDto = new SchemaDto(UUID.randomUUID(), null, null, null, null, true, fieldDtos, null, null, null);
        return List.of(schemaDto);
    }
}
