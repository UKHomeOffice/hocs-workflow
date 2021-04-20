package uk.gov.digital.ho.hocs.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UserDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.*;
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

    private static final String COMPONENT_ENTITY_LIST = "entity-list";
    private static final String COMPONENT_DROPDOWN = "dropdown";
    private static final String CHOICES_PROPERTY = "choices";
    private static final String CONTENT_TYPE_TEAMS = "TEAMS";
    private static final String CONTENT_TYPE_USERS = "USERS";
    private static final String ENTITY_PROPERTY = "entity";
    private static final String ENTITY_TYPE_DOCUMENT = "document";

    private static final String DOCUMENT_NOT_FOUND = "Document not found";

    @Autowired
    public WorkflowService(CaseworkClient caseworkClient,
                           DocumentClient documentClient,
                           InfoClient infoClient,
                           CamundaClient camundaClient) {
        this.caseworkClient = caseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
    }

    public CreateCaseResponse createCase(String caseDataType, LocalDate dateReceived, List<DocumentSummary> documents, UUID userUUID) {
        // Create a case in the casework service in order to get a reference back to display to the user.
        Map<String, String> data = new HashMap<>();
        data.put(WorkflowConstants.DATE_RECEIVED, dateReceived.toString());
        data.put(WorkflowConstants.LAST_UPDATED_BY_USER, userUUID.toString());
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseDataType, data, dateReceived);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {

            // Add Documents to the case
            createDocument(caseUUID, documents);

            // Start a new camunda workflow (caseUUID is the business key).
            Map<String, String> seedData = new HashMap<>();
            seedData.put(WorkflowConstants.CASE_REFERENCE, caseResponse.getReference());
            seedData.putAll(data);
            camundaClient.startCase(caseUUID, caseDataType, seedData);

        } else {
            log.error("Failed to start case, invalid caseUUID!, event: {}", value(EVENT, CASE_STARTED_FAILURE));
            throw new ApplicationExceptions.EntityCreationException("Failed to start case, invalid caseUUID!", CASE_STARTED_FAILURE);
        }
        return new CreateCaseResponse(caseUUID, caseResponse.getReference());
    }

    public void createDocument(UUID caseUUID, List<DocumentSummary> documents) {
        if (documents != null) {
            // Add any Documents to the case
            for (DocumentSummary document : documents) {
                documentClient.createDocument(caseUUID, document.getDisplayName(), document.getS3UntrustedUrl(), document.getType());
            }
        }
    }

    public void createExemption(UUID caseUUID, String exemption) {
        if (exemption != null) {
            // Add exemption to the case
            System.out.println("Case: " + caseUUID + " adding exemption: " + exemption);

            caseworkClient.createExemption(caseUUID, exemption);
        }
    }

    public GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getStageScreenName(stageUUID);

        if (!screenName.equals("FINISH")) {

            GetCaseworkCaseDataResponse inputResponse = caseworkClient.getCase(caseUUID);

            SchemaDto schemaDto = infoClient.getSchema(screenName);
            List<HocsFormField> fields = schemaDto.getFields().stream().map(HocsFormField::from).collect(toList());
            List<HocsFormSecondaryAction> secondaryActions = schemaDto.getSecondaryActions().stream().map(HocsFormSecondaryAction::from).collect(toList());
            fields = HocsFormAccordion.loadFormAccordions(fields);
            HocsSchema schema = new HocsSchema(schemaDto.getTitle(), schemaDto.getDefaultActionLabel(), fields, secondaryActions, schemaDto.getProps());
            HocsForm form = new HocsForm(schema, inputResponse.getData());
            return new GetStageResponse(stageUUID, inputResponse.getReference(), form, inputResponse.getType());
        } else {
            return new GetStageResponse(stageUUID, null, null, null);
        }
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

        return new GetCaseResponse(inputResponse.getReference(), schema, dataMap, inputResponse.getType());
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

        HocsSchema hocsSchema = new HocsSchema(inputResponse.getReference(), null, fieldsToAdd, null, null);

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
}
