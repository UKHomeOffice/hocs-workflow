package uk.gov.digital.ho.hocs.workflow.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseworkCorrespondentRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDtoBuilder;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetCaseDetailsResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetProcessVariablesResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.ProcessVariables;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponseBuilder;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetStageResponseDto;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.MigrateCaseworkCaseRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.MigrateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.StageDto;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.dto.CreateCaseworkDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormAccordion;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormSecondaryAction;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Field;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Schema;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.SecondaryAction;
import uk.gov.digital.ho.hocs.workflow.helper.SchemaDtoBuilder;
import uk.gov.digital.ho.hocs.workflow.security.UserPermissionsService;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowServiceTest {

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

    private final Object[] fieldValidation = new Object[] { };

    private final String[] secondaryActionValidation = new String[] { };

    private final Object schemaDtoValidation = new Object();

    private final Object schemaDtoProps = new Object();

    private final UUID oldTeam = UUID.fromString("141a19e7-4cee-40d7-b078-50fc43846ca3");

    private final UUID caseUUID = UUID.fromString("8ecc4f69-b64a-4825-afbf-31f5af95d292");

    private final String stageUUID = "5b1c9476-2c0d-40d7-a814-f83e1d5ab8df";

    private final Field childField = new Field("test", "test", "test", fieldValidation, new HashMap<>(), null);

    @Captor
    ArgumentCaptor<CreateCaseworkCorrespondentRequest> argumentCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> caseDateArgumentCaptor;

    @Captor
    ArgumentCaptor<Map<String, String>> seedDataArgumentCaptor;

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private DocumentClient documentClient;

    @Mock
    private InfoClient infoClient;

    @Mock
    private FormService formService;

    @Mock
    private UserPermissionsService userPermissionsService;

    @Spy
    @InjectMocks
    private WorkflowService workflowServiceSpy;

    private WorkflowService workflowService;

    @Before
    public void beforeTest() {
        workflowService = new WorkflowService(caseworkClient, documentClient, infoClient, camundaClient,
            userPermissionsService, formService);
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
    public void createCase_whenNoInitialCorrespondentDetails() {

        String caseDataType = "FOI";
        LocalDate dateReceived = LocalDate.EPOCH;
        List<DocumentSummary> documents = new ArrayList<>();
        UUID userUUID = UUID.randomUUID();
        Map<String, String> receivedData = new HashMap<>();
        CreateCaseworkCaseResponse createCaseworkCaseResponse = new CreateCaseworkCaseResponse(UUID.randomUUID(), null,
            Map.of());

        when(caseworkClient.createCase(any(), any(), any(), any())).thenReturn(createCaseworkCaseResponse);

        CreateCaseResponse output = workflowService.createCase(caseDataType, dateReceived, documents, userUUID, null,
            receivedData);
        assertThat(output.getUuid()).isNotNull();
        verify(camundaClient, times(1)).startCase(any(), any(), any());
        verify(caseworkClient, times(0)).saveCorrespondent(any(), any(), any());
    }

    @Test
    public void createCaseSendsSeedDataToCamundaForCompWebFormCase() {
        String caseDataType = "COMP";
        LocalDate dateReceived = LocalDate.EPOCH;
        List<DocumentSummary> documents = new ArrayList<>();
        UUID userUUID = UUID.randomUUID();
        Map<String, String> receivedData = Map.of(WorkflowConstants.CHANNEL, WorkflowConstants.CHANNEL_COMP_WEBFORM,
            WorkflowConstants.CHANNEL_COMP_ORIGINATEDFROM, WorkflowConstants.CHANNEL_COMP_WEBFORM);
        CreateCaseworkCaseResponse createCaseworkCaseResponse = new CreateCaseworkCaseResponse(UUID.randomUUID(), null,
            Map.of());

        when(caseworkClient.createCase(any(), caseDateArgumentCaptor.capture(), any(), any())).thenReturn(
            createCaseworkCaseResponse);

        CreateCaseResponse output = workflowService.createCase(caseDataType, dateReceived, documents, userUUID, null,
            receivedData);
        assertThat(output.getUuid()).isNotNull();
        verify(camundaClient, times(1)).startCase(any(), any(), seedDataArgumentCaptor.capture());
        verify(caseworkClient, times(0)).saveCorrespondent(any(), any(), any());

        assertThat(seedDataArgumentCaptor.getValue()).containsAllEntriesOf(receivedData);
        assertThat(seedDataArgumentCaptor.getValue()).containsAllEntriesOf(receivedData);
    }

    @Test
    public void createDocument() {
        // given
        UUID caseUuid = UUID.randomUUID();
        UUID actionDataItemUuid = UUID.randomUUID();

        final List<DocumentSummary> documentSummaries = List.of(new DocumentSummary("displayName1", "type1", "url1"),
            new DocumentSummary("displayName2", "type2", "url2"));

        // when
        workflowService.createDocument(caseUuid, actionDataItemUuid, documentSummaries);

        // then
        verify(documentClient, times(2)).createDocument(eq(caseUuid), any(CreateCaseworkDocumentRequest.class));
    }

    @Test
    public void createCase_whenHasInitialEmailCorrespondentDetails() {

        String expectedReference = "REFERENCE";
        String expectedCountry = "United Kingdom";
        String expectedEmail = "test@test.com";
        String expectedFullname = "John Doe";

        String caseDataType = "FOI";
        LocalDate dateReceived = LocalDate.EPOCH;
        List<DocumentSummary> documents = new ArrayList<>();
        UUID userUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> receivedData = new HashMap<>();
        receivedData.put("Fullname", expectedFullname);
        receivedData.put("Email", expectedEmail);
        receivedData.put("Country", expectedCountry);
        receivedData.put("Reference", expectedReference);
        CreateCaseworkCaseResponse createCaseworkCaseResponse = new CreateCaseworkCaseResponse(caseUUID, null,
            Map.of());

        when(caseworkClient.createCase(any(), any(), any(), any())).thenReturn(createCaseworkCaseResponse);
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(
            Optional.of(new StageDto(UUID.randomUUID(), "RANDOM_STAGE", UUID.randomUUID())));

        CreateCaseResponse output = workflowService.createCase(caseDataType, dateReceived, documents, userUUID, null,
            receivedData);
        assertThat(output.getUuid()).isNotNull();
        verify(camundaClient, times(1)).startCase(any(), any(), any());
        verify(caseworkClient, times(1)).saveCorrespondent(any(), any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getFullname()).isEqualTo(expectedFullname);
        assertThat(argumentCaptor.getValue().getEmail()).isEqualTo(expectedEmail);
        assertThat(argumentCaptor.getValue().getCountry()).isEqualTo(expectedCountry);
        assertThat(argumentCaptor.getValue().getReference()).isEqualTo(expectedReference);
    }

    @Test
    public void createCase_whenHasInitialPostCorrespondentDetails() {

        String expectedReference = "REFERENCE";
        String expectedCountry = "United Kingdom";
        String expectedEmail = "test@test.com";
        String expectedFullname = "John Doe";
        String expectedAddress1 = "Building";
        String expectedAddress2 = "Street";
        String expectedAddress3 = "Town Or City";
        String expectedPostcode = "TE5 7ER";
        String expectedTelephone = "01234567890";

        String caseDataType = "FOI";
        LocalDate dateReceived = LocalDate.EPOCH;
        List<DocumentSummary> documents = new ArrayList<>();
        UUID userUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> receivedData = new HashMap<>();
        receivedData.put("Fullname", expectedFullname);
        receivedData.put("Address1", expectedAddress1);
        receivedData.put("Address2", expectedAddress2);
        receivedData.put("Address3", expectedAddress3);
        receivedData.put("Postcode", expectedPostcode);
        receivedData.put("Telephone", expectedTelephone);
        receivedData.put("Email", expectedEmail);
        receivedData.put("Country", expectedCountry);
        receivedData.put("Reference", expectedReference);
        CreateCaseworkCaseResponse createCaseworkCaseResponse = new CreateCaseworkCaseResponse(caseUUID, null,
            Map.of());

        when(caseworkClient.createCase(any(), any(), any(), any())).thenReturn(createCaseworkCaseResponse);
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(
            Optional.of(new StageDto(UUID.randomUUID(), "RANDOM_STAGE", UUID.randomUUID())));

        CreateCaseResponse output = workflowService.createCase(caseDataType, dateReceived, documents, userUUID, null,
            receivedData);
        assertThat(output.getUuid()).isNotNull();
        verify(camundaClient, times(1)).startCase(any(), any(), any());
        verify(caseworkClient, times(1)).saveCorrespondent(any(), any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getFullname()).isEqualTo(expectedFullname);
        assertThat(argumentCaptor.getValue().getAddress1()).isEqualTo(expectedAddress1);
        assertThat(argumentCaptor.getValue().getAddress2()).isEqualTo(expectedAddress2);
        assertThat(argumentCaptor.getValue().getAddress3()).isEqualTo(expectedAddress3);
        assertThat(argumentCaptor.getValue().getPostcode()).isEqualTo(expectedPostcode);
        assertThat(argumentCaptor.getValue().getTelephone()).isEqualTo(expectedTelephone);
        assertThat(argumentCaptor.getValue().getEmail()).isEqualTo(expectedEmail);
        assertThat(argumentCaptor.getValue().getCountry()).isEqualTo(expectedCountry);
        assertThat(argumentCaptor.getValue().getReference()).isEqualTo(expectedReference);
        caseworkClient.getCorrespondentsForCase(caseUUID);
    }

    @Test
    public void createCase_whenHasOrganisationCorrespondentDetails() {

        String expectedReference = "REFERENCE";
        String expectedCountry = "United Kingdom";
        String expectedEmail = "test@test.com";
        String expectedFullname = "John Doe";
        String expectedOrganisation = "A Large Organisation";
        String expectedAddress1 = "Building";
        String expectedAddress2 = "Street";
        String expectedAddress3 = "Town Or City";
        String expectedPostcode = "TE5 7ER";
        String expectedTelephone = "01234567890";

        String caseDataType = "FOI";
        LocalDate dateReceived = LocalDate.EPOCH;
        List<DocumentSummary> documents = new ArrayList<>();
        UUID userUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> receivedData = new HashMap<>();
        receivedData.put("Fullname", expectedFullname);
        receivedData.put("Organisation", expectedOrganisation);
        receivedData.put("Address1", expectedAddress1);
        receivedData.put("Address2", expectedAddress2);
        receivedData.put("Address3", expectedAddress3);
        receivedData.put("Postcode", expectedPostcode);
        receivedData.put("Telephone", expectedTelephone);
        receivedData.put("Email", expectedEmail);
        receivedData.put("Country", expectedCountry);
        receivedData.put("Reference", expectedReference);
        CreateCaseworkCaseResponse createCaseworkCaseResponse = new CreateCaseworkCaseResponse(caseUUID, null,
            Map.of());

        when(caseworkClient.createCase(any(), any(), any(), any())).thenReturn(createCaseworkCaseResponse);
        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(
            Optional.of(new StageDto(UUID.randomUUID(), "RANDOM_STAGE", UUID.randomUUID())));

        CreateCaseResponse output = workflowService.createCase(caseDataType, dateReceived, documents, userUUID, null,
            receivedData);
        assertThat(output.getUuid()).isNotNull();
        verify(camundaClient, times(1)).startCase(any(), any(), any());
        verify(caseworkClient, times(1)).saveCorrespondent(any(), any(), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getFullname()).isEqualTo(expectedFullname);
        assertThat(argumentCaptor.getValue().getOrganisation()).isEqualTo(expectedOrganisation);
        assertThat(argumentCaptor.getValue().getAddress1()).isEqualTo(expectedAddress1);
        assertThat(argumentCaptor.getValue().getAddress2()).isEqualTo(expectedAddress2);
        assertThat(argumentCaptor.getValue().getAddress3()).isEqualTo(expectedAddress3);
        assertThat(argumentCaptor.getValue().getPostcode()).isEqualTo(expectedPostcode);
        assertThat(argumentCaptor.getValue().getTelephone()).isEqualTo(expectedTelephone);
        assertThat(argumentCaptor.getValue().getEmail()).isEqualTo(expectedEmail);
        assertThat(argumentCaptor.getValue().getCountry()).isEqualTo(expectedCountry);
        assertThat(argumentCaptor.getValue().getReference()).isEqualTo(expectedReference);
        caseworkClient.getCorrespondentsForCase(caseUUID);
    }

    @Test
    public void createCase_withCaseData() {

        String data1 = "data1";
        String data2 = "data2";

        String caseDataType = "CASE";
        LocalDate dateReceived = LocalDate.EPOCH;
        List<DocumentSummary> documents = new ArrayList<>();
        UUID userUUID = UUID.randomUUID();
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> receivedData = new HashMap<>();
        Map<String, String> caseData = Map.of("data1", data1, "data2", data2);

        CreateCaseworkCaseResponse createCaseworkCaseResponse = new CreateCaseworkCaseResponse(caseUUID, null,
            caseData);

        when(caseworkClient.createCase(any(), any(), any(), any())).thenReturn(createCaseworkCaseResponse);

        CreateCaseResponse output = workflowService.createCase(caseDataType, dateReceived, documents, userUUID, null,
            receivedData);
        assertThat(output.getUuid()).isNotNull();
        verify(camundaClient, times(1)).startCase(any(), any(), seedDataArgumentCaptor.capture());
        assertThat(seedDataArgumentCaptor.getValue().get("data1")).isEqualTo(data1);
        assertThat(seedDataArgumentCaptor.getValue().get("data2")).isEqualTo(data2);
    }

    @Test
    public void migrateCase() {
        String caseDataType = "BF";
        UUID fromCaseUUID = UUID.randomUUID();
        UUID toCaseUUID = UUID.randomUUID();
        Map<String, String> caseDataMap = new HashMap<>();
        MigrateCaseworkCaseResponse migrateCaseworkCaseResponse = new MigrateCaseworkCaseResponse(toCaseUUID,
            caseDataMap);

        ArgumentCaptor<MigrateCaseworkCaseRequest> argumentCaptor = ArgumentCaptor.forClass(
            MigrateCaseworkCaseRequest.class);
        when(caseworkClient.migrateCase(eq(fromCaseUUID), any(MigrateCaseworkCaseRequest.class))).thenReturn(
            migrateCaseworkCaseResponse);

        workflowService.migrateCase(caseDataType, fromCaseUUID);

        verify(camundaClient, times(1)).startCase(toCaseUUID, caseDataType, caseDataMap);
        verify(caseworkClient, times(1)).migrateCase(eq(fromCaseUUID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getType()).isEqualTo(caseDataType);

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

        when(documentClient.getDocumentName(testDocumentUuid)).thenThrow(
            new HttpClientErrorException(HttpStatus.NOT_FOUND));

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

        when(documentClient.getDocumentName(testDocumentUuid)).thenThrow(
            new HttpClientErrorException(HttpStatus.FORBIDDEN));

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

        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = GetCaseworkCaseDataResponseBuilder.aGetCaseworkCaseDataResponse().withUuid(
            caseUUID).withType(caseType).withData(data).withReference(caseRef).build();

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

        FieldDto fieldDtoA = FieldDtoBuilder.aFieldDto().withName(nameA).withComponent(componentA).withProps(
            propertyA).build();
        FieldDto fieldDtoB = FieldDtoBuilder.aFieldDto().withName(nameB).withComponent(componentB).withProps(
            propertyB).build();

        SchemaDto schemaDto = new SchemaDtoBuilder().withFields(List.of(fieldDtoA, fieldDtoB)).build();

        when(caseworkClient.getFullCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);
        when(caseworkClient.getAllStagesForCase(caseUUID)).thenReturn(allStagesForCaseResponse);
        when(infoClient.getCaseDetailsFieldsByCaseType(caseType)).thenReturn(
            List.of(caseDetailsFieldDtoA, caseDetailsFieldDtoB));
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

        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = GetCaseworkCaseDataResponseBuilder.aGetCaseworkCaseDataResponse().withUuid(
            caseUUID).withType(caseType).withData(data).withReference(caseRef).build();

        StageDto stageDto1 = new StageDto(UUID.randomUUID(), stageType1, UUID.randomUUID());
        StageDto stageDto2 = new StageDto(UUID.randomUUID(), stageType2, UUID.randomUUID());
        List<StageDto> stageDtos = new ArrayList<StageDto>();
        stageDtos.add(stageDto1);
        stageDtos.add(stageDto2);
        GetAllStagesForCaseResponse allStagesForCaseResponse = new GetAllStagesForCaseResponse(stageDtos);

        FieldDto fieldDtoA = FieldDtoBuilder.aFieldDto().withName(nameA).withComponent(componentA).withProps(
            propertyA).build();
        FieldDto fieldDtoB = FieldDtoBuilder.aFieldDto().withName(nameB).withComponent(componentB).withProps(
            propertyB).build();
        SchemaDto schemaDto1 = new SchemaDtoBuilder().withFields(List.of(fieldDtoA, fieldDtoB)).withStageType(
            stageType1).build();

        FieldDto fieldDto1 = FieldDtoBuilder.aFieldDto().withName(nameA).withComponent(componentA).withProps(
            propertyA).build();
        FieldDto fieldDto2 = FieldDtoBuilder.aFieldDto().withName(nameB).withComponent(componentB).withProps(
            propertyB).build();
        SchemaDto schemaDto2 = new SchemaDtoBuilder().withFields(List.of(fieldDto1, fieldDto2)).withStageType(
            stageType2).build();

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
        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = new GetCaseworkCaseDataResponse(caseUUID, null, null,
            caseRef, caseResponseData, null, null, null, null, null, null, null);
        when(caseworkClient.getCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);
        when(formService.getFormSchema(screenName)).thenReturn(exampleSchemaDto());

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
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getComponent()).isSameAs(
            secondaryActionComponent);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getValidation()).isSameAs(
            secondaryActionValidation);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getProps().get("label")).isSameAs(
            secondaryActionLabel);
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getProps().get("name")).isSameAs(
            secondaryActionName);
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
        when(camundaClient.hasProcessInstanceVariableWithValue(anyString(), anyString(), anyString())).thenReturn(
            false);

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
        when(camundaClient.hasProcessInstanceVariableWithValue(caseUUID.toString(), "STICKY_CASES", "true")).thenReturn(
            true);

        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = new GetCaseworkCaseDataResponse(caseUUID, null, null,
            caseRef, caseResponseData, null, null, null, null, null, null, null);
        when(caseworkClient.getCase(caseUUID)).thenReturn(getCaseworkCaseDataResponse);

        //when
        GetStageResponse response = workflowService.getStage(caseUUID, stageUUID);

        //then
        assertThat(response.getStageUUID()).isEqualTo(nextStageUUID);
        assertThat(response.getForm()).isNotNull();
        assertThat(response.getCaseReference()).isNotNull();
        verify(caseworkClient).updateStageUser(caseUUID, nextStageUUID, userUUID);
        verify(camundaClient).removeProcessInstanceVariableFromAllScopes(caseUUID.toString(), nextStageUUID.toString(),
            "STICKY_CASES");
    }

    private HocsSchema exampleSchemaDto() {
        List<Field> fieldList = List.of(
            new Field(fieldName, fieldLabel, fieldComponent, fieldValidation, new HashMap<>(), childField));
        List<SecondaryAction> secondaryActionList = List.of(
            new SecondaryAction(secondaryActionName, secondaryActionLabel, secondaryActionComponent,
                secondaryActionValidation, new HashMap<>()));

        Schema schema = new Schema(schemaTitle, schemaDefaultActionLabel, fieldList, secondaryActionList,
            schemaDtoProps, schemaDtoValidation, null);

        List<HocsFormField> fields = schema.getFields().stream().map(HocsFormField::from).toList();
        fields = HocsFormAccordion.loadFormAccordions(fields);

        List<HocsFormSecondaryAction> secondaryActions = schema.getSecondaryActions().stream().map(
            HocsFormSecondaryAction::from).toList();
        return new HocsSchema(schema.getTitle(), schema.getDefaultActionLabel(), fields, secondaryActions,
            schema.getProps(), schema.getValidation(), schema.getSummary());
    }

    private List<SchemaDto> setupTestSchemas() {
        Map<String, Object> props = Map.of("entity", "document");
        List<FieldDto> fieldDtos = List.of(
            new FieldDto(null, testFieldName, null, "entity-list", null, props, true, null));
        SchemaDto schemaDto = new SchemaDto(UUID.randomUUID(), null, null, null, null, true, fieldDtos, null, null,
            null, null);
        return List.of(schemaDto);
    }

    @Test
    public void closeCase() throws JsonProcessingException, UnsupportedEncodingException {
        setupCloseCase();

        ResponseEntity response = workflowService.closeCase(caseUUID);
        assertThat(response.getStatusCode() == HttpStatus.OK);
    }

    @Test
    public void closeCaseBreakAtUpdateTeam() throws JsonProcessingException, UnsupportedEncodingException {
        setupCloseCase();
        doThrow(new RuntimeException()).when(caseworkClient).updateStageTeam(any(), any(), any(), any());

        ResponseEntity response = workflowServiceSpy.closeCase(caseUUID);

        assertThat(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.verify(caseworkClient).completeCase(caseUUID, false);
        Mockito.verify(workflowServiceSpy, never()).deleteProcess(any());
    }

    @Test
    public void closeCaseBreakAtProcessStop() throws JsonProcessingException, UnsupportedEncodingException {
        setupCloseCase();
        doThrow(new RuntimeException()).when(workflowServiceSpy).deleteProcess(any());

        ResponseEntity response = workflowServiceSpy.closeCase(caseUUID);

        assertThat(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.verify(caseworkClient).completeCase(caseUUID, false);
        Mockito.verify(caseworkClient).updateStageTeam(caseUUID, UUID.fromString(stageUUID), oldTeam, null);
    }

    @Test
    public void updateCaseDataValues_ActiveStages() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseDataType = "CASE_DATA";
        Map<String, String> request = new HashMap<>();
        String msg = "Case data updated";

        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.of(new StageDto()));

        workflowService.updateCaseDataValues(caseUUID, stageUUID, caseDataType, request);

        verify(caseworkClient, times(1)).getActiveStage(caseUUID);
        verify(camundaClient, times(1)).updateTask(stageUUID, request);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, request);
        verify(caseworkClient, times(1)).createCaseNote(caseUUID, caseDataType, msg);

        verifyNoMoreInteractions(caseworkClient, camundaClient);

    }

    @Test
    public void updateCaseDataValues_NoActiveStages() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseDataType = "CASE_DATA";
        Map<String, String> request = new HashMap<>();
        String msg = "Case data updated";

        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.empty());

        workflowService.updateCaseDataValues(caseUUID, stageUUID, caseDataType, request);

        verify(caseworkClient, times(1)).getActiveStage(caseUUID);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, request);
        verify(caseworkClient, times(1)).createCaseNote(caseUUID, caseDataType, msg);

        verifyNoMoreInteractions(caseworkClient);

        verifyNoInteractions(camundaClient);
    }

    private void setupCloseCase() throws UnsupportedEncodingException {
        GetCaseworkCaseDataResponse getCaseworkCaseDataResponse = new GetCaseworkCaseDataResponse();
        getCaseworkCaseDataResponse.setReference("caseref");
        getCaseworkCaseDataResponse.setCompleted(false);
        Mockito.doReturn(getCaseworkCaseDataResponse).when(caseworkClient).getCase(any());

        GetStageResponseDto getStageResponseDto = new GetStageResponseDto(UUID.fromString(stageUUID), null, null);
        Set<GetStageResponseDto> stages = new HashSet<>(Collections.singletonList(getStageResponseDto));
        GetStagesResponse getStagesResponse = new GetStagesResponse(stages);

        Mockito.doReturn(getStagesResponse).when(caseworkClient).getActiveStage(anyString());

        Mockito.doReturn(oldTeam).when(caseworkClient).getStageTeam(any(), any());
        Mockito.doReturn(null).when(caseworkClient).updateStageTeam(any(), any(), any(), any());
        Mockito.doNothing().when(caseworkClient).completeCase(any(), anyBoolean());
        when(workflowService.closeCase(any())).thenCallRealMethod();
    }

    @Test
    public void getAllProcessVariablesForCase_mapsCamundaDataToTheExpectedDTO() {
        // given
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        StageDto stageDto = mock(StageDto.class);
        when(stageDto.getUuid()).thenReturn(stageUUID);

        List<ProcessVariables> processVariables = List.of(
            mock(ProcessVariables.class),
            mock(ProcessVariables.class)
        );

        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.of(stageDto));
        when(camundaClient.getProcessVariablesForCase(caseUUID, stageUUID)).thenReturn(processVariables);

        // when
        GetProcessVariablesResponse dto = workflowService.getAllProcessVariablesForCase(caseUUID);

        //then
        assertThat(dto.caseUUID()).isEqualTo(caseUUID);
        assertThat(dto.stageUUID()).isEqualTo(stageUUID);
        assertThat(dto.processes()).isSameAs(processVariables);
    }

    @Test
    public void getAllProcessVariablesForCase_mapsCamundaDataToTheExpectedDTO_whenThereIsNoActiveStage() {
        // given
        UUID caseUUID = UUID.randomUUID();

        List<ProcessVariables> processVariables = List.of(mock(ProcessVariables.class));

        when(caseworkClient.getActiveStage(caseUUID)).thenReturn(Optional.empty());
        when(camundaClient.getProcessVariablesForCase(caseUUID, null)).thenReturn(processVariables);

        // when
        GetProcessVariablesResponse dto = workflowService.getAllProcessVariablesForCase(caseUUID);

        //then
        assertThat(dto.caseUUID()).isEqualTo(caseUUID);
        assertThat(dto.stageUUID()).isEqualTo(null);
        assertThat(dto.processes()).isSameAs(processVariables);
    }

    @Test
    public void getVariablesForProcessInstanceId_passesRequestAndResponseDirectly() {
        String processInstanceId = "process-instance-id";
        ProcessVariables processVariables = mock(ProcessVariables.class);
        when(camundaClient.getProcessVariablesForInstance(processInstanceId)).thenReturn(processVariables);

        ProcessVariables result = workflowService.getProcessVariablesForInstance(processInstanceId);

        assertThat(result).isEqualTo(processVariables);
    }

    @Test
    public void updateProcessVariables_passesTheArgumentsToCamundaClient() {
        String processInstanceId = "process-instance-id";
        Map<String, String> variables = new HashMap<>(){
            {
                put("present", "String");
                put("empty",  null);
            }
        };

        workflowService.updateProcessVariables(processInstanceId, variables);

        verify(camundaClient).updateProcessVariables(eq(processInstanceId), eq(variables));
    }

}
