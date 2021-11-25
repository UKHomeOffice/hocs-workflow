package uk.gov.digital.ho.hocs.workflow.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.StageDto;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UserDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.*;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseworkCorrespondentRequest;
import uk.gov.digital.ho.hocs.workflow.security.UserPermissionsService;
import uk.gov.digital.ho.hocs.workflow.util.UuidUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CASE_STARTED_FAILURE;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;

@Service
@Slf4j
public class WorkflowService {

    private final CaseworkClient caseworkClient;
    private final DocumentClient documentClient;
    private final InfoClient infoClient;
    private final CamundaClient camundaClient;
    private final UserPermissionsService userPermissionsService;

    private static final String COMPONENT_ENTITY_LIST = "entity-list";
    private static final String COMPONENT_DROPDOWN = "dropdown";
    private static final String CHOICES_PROPERTY = "choices";
    private static final String CONTENT_TYPE_TEAMS = "TEAMS";
    private static final String CONTENT_TYPE_USERS = "USERS";
    private static final String ENTITY_PROPERTY = "entity";
    private static final String ENTITY_TYPE_DOCUMENT = "document";

    private static final String DOCUMENT_NOT_FOUND = "Document not found";

    public static final String STICKY_CASES_VARIABLE = "STICKY_CASES";

    @Autowired
    public WorkflowService(CaseworkClient caseworkClient,
                           DocumentClient documentClient,
                           InfoClient infoClient,
                           CamundaClient camundaClient,
                           UserPermissionsService userPermissionsService) {
        this.caseworkClient = caseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
        this.userPermissionsService = userPermissionsService;
    }

    public CreateCaseResponse createCase(String caseDataType, LocalDate dateReceived, List<DocumentSummary> documents, UUID userUUID, UUID fromCaseUUID, Map<String, String> receivedData) {
        // Create a case in the casework service in order to get a reference back to display to the user.
        CreateCaseworkCorrespondentRequest correspondentRequest = null;

        Map<String, String> caseData = new HashMap<>();
        caseData.put(WorkflowConstants.DATE_RECEIVED, dateReceived.toString());
        caseData.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());

        if (receivedData != null && Objects.equals(caseDataType, WorkflowConstants.CASE_DATA_TYPE_FOI)) {
            // If we have a name we have correspondent details attached to the case creation request
            if(receivedData.containsKey(WorkflowConstants.FULL_NAME)) {
                correspondentRequest = buildCorrespondentRequest(receivedData);

                caseData.put(WorkflowConstants.KIMU_DATE_RECEIVED, receivedData.get(WorkflowConstants.KIMU_DATE_RECEIVED));
                caseData.put(WorkflowConstants.TOPICS, receivedData.get(WorkflowConstants.TOPICS));
                caseData.put(WorkflowConstants.ORIGINAL_CHANNEL, receivedData.get(WorkflowConstants.ORIGINAL_CHANNEL));
                caseData.put(WorkflowConstants.REQUEST_QUESTION, receivedData.get(WorkflowConstants.REQUEST_QUESTION));
            }
        }

        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseDataType, caseData, dateReceived, fromCaseUUID);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {
            // Add Documents to the case
            createDocument(caseUUID, null, documents);

            // Start a new camunda workflow (caseUUID is the business key).
            Map<String, String> seedData = new HashMap<>();
            if (Objects.equals(caseDataType, WorkflowConstants.CASE_DATA_TYPE_FOI) && receivedData != null) {
                seedData.put(WorkflowConstants.CASE_REFERENCE, caseResponse.getReference());
                seedData.put(WorkflowConstants.DATE_RECEIVED, dateReceived.toString());
                seedData.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());
                seedData.put(WorkflowConstants.KIMU_DATE_RECEIVED, receivedData.get(WorkflowConstants.KIMU_DATE_RECEIVED));
                seedData.put(WorkflowConstants.TOPICS, receivedData.get(WorkflowConstants.TOPICS));
                seedData.put(WorkflowConstants.REQUEST_QUESTION, receivedData.get(WorkflowConstants.REQUEST_QUESTION));
                seedData.put(WorkflowConstants.ORIGINAL_CHANNEL, receivedData.get(WorkflowConstants.ORIGINAL_CHANNEL));
            } else {
                seedData.put(WorkflowConstants.CASE_REFERENCE, caseResponse.getReference());
                seedData.put(WorkflowConstants.DATE_RECEIVED, dateReceived.toString());
                seedData.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());
            }

            camundaClient.startCase(caseUUID, caseDataType, seedData);
            //Create correspondent
            if (correspondentRequest != null) {
                UUID stageUUID = caseworkClient.getStageUUID(caseUUID);
                caseworkClient.saveCorrespondent(caseUUID, stageUUID, correspondentRequest);
            }

        } else {
            log.error("Failed to start case, invalid caseUUID!, event: {}", value(EVENT, CASE_STARTED_FAILURE));
            throw new ApplicationExceptions.EntityCreationException("Failed to start case, invalid caseUUID!", CASE_STARTED_FAILURE);
        }
        return new CreateCaseResponse(caseUUID, caseResponse.getReference());
    }

    private CreateCaseworkCorrespondentRequest buildCorrespondentRequest(Map<String, String> data) {
        CreateCaseworkCorrespondentRequest correspondentRequest;
        correspondentRequest = CreateCaseworkCorrespondentRequest.builder()
                .type(WorkflowConstants.TYPE_FOI_REQUESTER)
                .fullname(data.get(WorkflowConstants.FULL_NAME))
                .postcode(data.get(WorkflowConstants.POSTCODE))
                .organisation(data.get(WorkflowConstants.ORGANISATION))
                .address1(data.get(WorkflowConstants.ADDRESS1))
                .address2(data.get(WorkflowConstants.ADDRESS2))
                .address3(data.get(WorkflowConstants.ADDRESS3))
                .country(data.get(WorkflowConstants.COUNTRY))
                .telephone(data.get(WorkflowConstants.TELEPHONE))
                .email(data.get(WorkflowConstants.EMAIL))
                .reference(data.get(WorkflowConstants.REFERENCE))
                .build();
        return correspondentRequest;
    }

    public void createDocument(UUID caseUUID, UUID actionDataItemUuid, List<DocumentSummary> documents) {
        if (documents != null) {
            // Add any Documents to the case
            for (DocumentSummary document : documents) {
                documentClient.createDocument(
                        caseUUID,
                        actionDataItemUuid,
                        document.getDisplayName(),
                        document.getS3UntrustedUrl(),
                        document.getType()
                );
            }
        }

    }
    public void createDocument(UUID caseUUID, List<DocumentSummary> documents) {
        if (documents != null) {
            // Add any Documents to the case
            for (DocumentSummary document : documents) {
                documentClient.createDocument(
                        caseUUID,
                        document.getDisplayName(),
                        document.getS3UntrustedUrl(),
                        document.getType()
                );
            }
        }
    }

    public GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getStageScreenName(stageUUID);

        if (screenName.equals("FINISH")) {
            Optional<StageDto> maybeActiveStage = caseworkClient.getActiveStage(caseUUID);
            if (maybeActiveStage.isPresent()) {
                StageDto nextStage = maybeActiveStage.get();
                UUID nextStageId = nextStage.getUuid();
                boolean isUserOnTheTeamForNextStage = userPermissionsService.isUserOnTeam(nextStage.getTeamUUID());
                boolean isStickyCasesModeOn = isStickyCasesModeOn(caseUUID);
                if (isStickyCasesModeOn && isUserOnTheTeamForNextStage) {
                    //assign user to the next stage
                    caseworkClient.updateStageUser(caseUUID, nextStageId, userPermissionsService.getUserId());

                    //turn off sticky cases
                    turnOffStickyCases(caseUUID, nextStageId);

                    //return information about the screen on the next stage
                    return getStage(caseUUID, nextStageId);
                }
            }
            return new GetStageResponse(stageUUID, null, null);
        }

        GetCaseworkCaseDataResponse inputResponse = caseworkClient.getCase(caseUUID);

        SchemaDto schemaDto = infoClient.getSchema(screenName);
        List<HocsFormField> fields = schemaDto.getFields().stream().map(HocsFormField::from).collect(toList());
        List<HocsFormSecondaryAction> secondaryActions = schemaDto.getSecondaryActions().stream().map(HocsFormSecondaryAction::from).collect(toList());
        fields = HocsFormAccordion.loadFormAccordions(fields);
        HocsSchema schema = new HocsSchema(schemaDto.getTitle(), schemaDto.getDefaultActionLabel(), fields, secondaryActions, schemaDto.getProps(), schemaDto.getValidation());
        HocsForm form = new HocsForm(schema, inputResponse.getData());
        return new GetStageResponse(stageUUID, inputResponse.getReference(), form);
    }

    private boolean isStickyCasesModeOn(UUID caseUUID) {
        return camundaClient.hasProcessInstanceVariableWithValue(caseUUID.toString(), STICKY_CASES_VARIABLE, Boolean.TRUE.toString());
    }

    private void turnOffStickyCases(UUID caseUUID, UUID stageUUID) {
        camundaClient.removeProcessInstanceVariableFromAllScopes(caseUUID.toString(), stageUUID.toString(), STICKY_CASES_VARIABLE);
    }

    public GetCaseResponse getAllCaseStages(UUID caseUUID) {

        GetCaseworkCaseDataResponse inputResponse = caseworkClient.getFullCase(caseUUID);

        GetAllStagesForCaseResponse allStagesForCase = caseworkClient.getAllStagesForCase(caseUUID);

        String caseStages = allStagesForCase.getStages()
                .stream()
                .map(s -> s.getType())
                .collect(Collectors.joining(","));

        List<SchemaDto> schemaDtos = infoClient.getSchemasForCaseTypeAndStages(inputResponse.getType(), caseStages);


        Map<String, List<SchemaDto>> stageSchemas = schemaDtos
                .stream()
                .collect(Collectors.groupingBy(SchemaDto::getStageType, LinkedHashMap::new, Collectors.toList()));


        Map<String, List<HocsFormField>> hocsFields = stageSchemas
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        c -> schemasToFormField(c.getValue()),
                        (k, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", k));
                        },
                        LinkedHashMap::new));

        HocsCaseSchema schema = new HocsCaseSchema("View Case", hocsFields);

        Map<String, String> dataMap = convertDataToSchema(schemaDtos, inputResponse.getData());

        return new GetCaseResponse(inputResponse.getReference(), schema, dataMap);
    }

    public GetCaseDetailsResponse getReadOnlyCaseDetails(UUID caseUUID) {
        GetCaseworkCaseDataResponse inputResponse = caseworkClient.getFullCase(caseUUID);

        List<CaseDetailsFieldDto> fields = infoClient.getCaseDetailsFieldsByCaseType(inputResponse.getType());
        List<HocsFormField> hocsFields = fields.stream().map(HocsFormField::from).collect(toList());
        List<HocsFormField> fieldsToAdd = HocsFormAccordion.loadFormAccordions(hocsFields);

        GetAllStagesForCaseResponse allStagesForCase = caseworkClient.getAllStagesForCase(caseUUID);

        String caseStages = allStagesForCase.getStages()
                .stream()
                .map(s -> s.getType())
                .collect(Collectors.joining(","));

        List<SchemaDto> schemaDtos = infoClient.getSchemasForCaseTypeAndStages(inputResponse.getType(), caseStages);

        HocsSchema hocsSchema = new HocsSchema(inputResponse.getReference(), null, fieldsToAdd, null, null, null);

        Map<String, String> dataMappings = convertDataToSchema(schemaDtos, inputResponse.getData());

        return new GetCaseDetailsResponse(hocsSchema, dataMappings);

    }

    public Map<String, String> convertDataToSchema(List<SchemaDto> schemaDtos, Map<String, String> dataMap) {
        for (SchemaDto schemaDto : schemaDtos) {
            for (FieldDto fieldDto : schemaDto.getFields()) {
                String keyString = fieldDto.getName();
                String uuidString = dataMap.getOrDefault(keyString, null);
                if (UuidUtils.isUUID(uuidString)) {
                    if (fieldDto.getComponent().equals(COMPONENT_DROPDOWN)) {
                        final Object choicesProperty = fieldDto.getProps().getOrDefault(CHOICES_PROPERTY, null);
                        if (choicesProperty != null) {
                            String choices = choicesProperty.toString();
                            if (choices.contains(CONTENT_TYPE_TEAMS)) {
                                TeamDto teamDto = infoClient.getTeam(UUID.fromString(uuidString));
                                if (teamDto != null) {
                                    dataMap.put(keyString, teamDto.getDisplayName());
                                }
                            } else if (choices.contains(CONTENT_TYPE_USERS)) {
                                final UserDto user = infoClient.getUser(UUID.fromString(uuidString));
                                if (user != null) {
                                    dataMap.put(keyString, user.displayFormat());
                                }
                            }
                        }
                    } else if (fieldDto.getComponent().equals(COMPONENT_ENTITY_LIST)) {
                        final Object entityProperty = fieldDto.getProps().getOrDefault(ENTITY_PROPERTY, null);
                        if (entityProperty != null) {
                            if (entityProperty.equals(ENTITY_TYPE_DOCUMENT)) {
                                dataMap.put(keyString, fetchDocumentName(UUID.fromString(uuidString)));
                            }
                        }
                    }
                }
            }
        }
        return dataMap;
    }

    private String fetchDocumentName(UUID documentUUID) {

        try {
            return documentClient.getDocumentName(documentUUID);
        } catch (HttpClientErrorException exception) {

            if (HttpStatus.NOT_FOUND.equals(exception.getStatusCode())) {
                log.warn("Document name not found for document {}", documentUUID);
                return DOCUMENT_NOT_FOUND;
            }
            throw exception;
        }


    }

    private static List<HocsFormField> schemasToFormField(List<SchemaDto> schemaDtos) {
        List<HocsFormField> fields = new ArrayList<>();
        Set<String> uniqueFieldNames = new HashSet<>();
        for (SchemaDto schemaDto : schemaDtos) {
            fields.add(HocsFormField.fromTitle(schemaDto.getTitle()));
            Collection<HocsFormField> fieldsToAdd = schemaDto.getFields().stream().map(HocsFormField::from).collect(toList());
            for (HocsFormField fieldToAdd : fieldsToAdd) {
                if (fieldToAdd.getProps().get("name") != null && !uniqueFieldNames.contains(String.valueOf(fieldToAdd.getProps().get("name")))) {
                    uniqueFieldNames.add(String.valueOf(fieldToAdd.getProps().get("name")));
                    fields.add(fieldToAdd);
                }
            }
        }
        return fields;
    }

    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values, String flowDirection, UUID userUUID) {

        values.put(WorkflowConstants.DIRECTION, flowDirection);
        values.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());

        if (Direction.FORWARD.getValue().equals(flowDirection)) {
            values.put(WorkflowConstants.VALID, "true");
            caseworkClient.updateCase(caseUUID, stageUUID, values);
        } else {
            values.put(WorkflowConstants.VALID, "false");
        }

        camundaClient.completeTask(stageUUID, values);
    }

    public ResponseEntity closeCase(UUID caseUUID) throws JsonProcessingException {
        GetCaseworkCaseDataResponse caseDetails = caseworkClient.getCase(caseUUID);

        //Get stage uuid from casework
        GetStagesResponse stage = caseworkClient.getActiveStage(caseDetails.getReference().replace("/", "%2F"));
        UUID stageUUID = stage.getStages().stream().findFirst().get().getStageUUID();

        //Mark case as complete
        caseworkClient.completeCase(caseUUID, true);

        //Update the stage team to null
        UUID oldTeam = caseworkClient.getStageTeam(caseUUID, stageUUID); //To allow reversion if required
        try {
            caseworkClient.updateStageTeam(caseUUID, stageUUID, null, null);
        } catch(Exception e) { //Revert marking as complete and return error
            caseworkClient.completeCase(caseUUID, caseDetails.getCompleted()); //Audit event may already have been created for closed case
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update stage team for " + caseUUID + " Error : " + e);
        }

        //Delete Camunda process
        try{
            deleteProcess(stageUUID);
        } catch(Exception e) { //Revert team change and case complete and return error
            caseworkClient.updateStageTeam(caseUUID, stageUUID, oldTeam, null);
            caseworkClient.completeCase(caseUUID, caseDetails.getCompleted()); //Audit event may already have been created for closed case
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete process for " + caseUUID + " Error : " + e);
        }
        return ResponseEntity.ok("Closed case " + caseUUID);
    }

    public void deleteProcess(UUID stageUuid){
        camundaClient.removeProcess(stageUuid);
    }

}
