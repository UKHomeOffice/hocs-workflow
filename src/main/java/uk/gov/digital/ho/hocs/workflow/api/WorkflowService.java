package uk.gov.digital.ho.hocs.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UserDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    public CreateCaseResponse createCase(String caseDataType, LocalDate dateReceived, List<DocumentSummary> documents) {
        // Create a case in the casework service in order to get a reference back to display to the user.
        Map<String, String> data = new HashMap<>();
        data.put("DateReceived", dateReceived.toString());
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseDataType, data, dateReceived);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {

            // Add Documents to the case
            createDocument(caseUUID, documents);

            // Start a new camunda workflow (caseUUID is the business key).
            Map<String, String> seedData = new HashMap<>();
            seedData.put("CaseReference",caseResponse.getReference());
            seedData.putAll(data);
            camundaClient.startCase(caseUUID, caseDataType, seedData);

        } else {
            log.error("Failed to start case, invalid caseUUID!", value(EVENT, CASE_STARTED_FAILURE));
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

    public GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getStageScreenName(stageUUID);

        if(!screenName.equals("FINISH")) {

            GetCaseworkCaseDataResponse inputResponse = caseworkClient.getCase(caseUUID);

            SchemaDto schemaDto = infoClient.getSchema(screenName);
            List<HocsFormField> fields = schemaDto.getFields().stream().map(HocsFormField::from).collect(Collectors.toList());
            List<HocsFormSecondaryAction> secondaryActions = schemaDto.getSecondaryActions().stream().map(HocsFormSecondaryAction::from).collect(Collectors.toList());
            fields = HocsFormAccordion.loadFormAccordions(fields);
            HocsSchema schema = new HocsSchema(schemaDto.getTitle(), schemaDto.getDefaultActionLabel(), fields, secondaryActions);
            HocsForm form = new HocsForm(schema,inputResponse.getData());
            return new GetStageResponse(stageUUID, inputResponse.getReference(), form);
        } else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    public GetCaseResponse getAllCaseStages(UUID caseUUID) {

            GetCaseworkCaseDataResponse inputResponse = caseworkClient.getFullCase(caseUUID);

            Set<SchemaDto> schemaDtos = infoClient.getSchemasForCaseType(inputResponse.getType());

            Map<String, List<SchemaDto>> stageSchemas = schemaDtos.stream().collect(Collectors.groupingBy(SchemaDto::getStageType));

            Map<String, List<HocsFormField>> hocsFields =  stageSchemas.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, c -> schemasToFormField(c.getValue())));

            HocsCaseSchema schema = new HocsCaseSchema("View Case", hocsFields);

            Map<String, String> dataMap = convertDataToSchema(schemaDtos, inputResponse.getData());

            return new GetCaseResponse(inputResponse.getReference(), schema, dataMap);
    }

    public Map<String, String> convertDataToSchema(Set<SchemaDto> schemaDtos, Map<String, String> dataMap){
        for(SchemaDto schemaDto : schemaDtos){
            for(FieldDto fieldDto : schemaDto.getFields()){
                String keyString = fieldDto.getName();
                String uuidString = dataMap.getOrDefault(keyString,null);
                if (uuidString != null && uuidString.contains(("-"))){
                    if (fieldDto.getComponent().equals(COMPONENT_DROPDOWN)){
                        final Object choicesProperty = fieldDto.getProps().getOrDefault(CHOICES_PROPERTY, null);
                        if (choicesProperty != null) {
                            String choices = choicesProperty.toString();
                            if (choices.contains(CONTENT_TYPE_TEAMS)){
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
                    } else if (fieldDto.getComponent().equals(COMPONENT_ENTITY_LIST)){
                        final Object entityProperty = fieldDto.getProps().getOrDefault(ENTITY_PROPERTY, null);
                        if (entityProperty != null){
                            if (entityProperty.equals(ENTITY_TYPE_DOCUMENT)){
                                String documentName = documentClient.getDocumentName(UUID.fromString(uuidString));
                                dataMap.put(keyString, documentName);
                            }
                        }
                    }
                }
            }
        }
        return dataMap;
    }

    private static List<HocsFormField> schemasToFormField(List<SchemaDto> schemaDtos) {
        List<HocsFormField> fields = new ArrayList<>();
        Set<String> uniqueFieldNames = new HashSet<>();
        for(SchemaDto schemaDto : schemaDtos) {
            fields.add(HocsFormField.fromTitle(schemaDto.getTitle()));
            Collection<HocsFormField> fieldsToAdd = schemaDto.getFields().stream().map(HocsFormField::from).collect(Collectors.toList());
            for(HocsFormField fieldToAdd : fieldsToAdd){
                if(fieldToAdd.getProps().get("name") != null && !uniqueFieldNames.contains(String.valueOf(fieldToAdd.getProps().get("name")))){
                    uniqueFieldNames.add(String.valueOf(fieldToAdd.getProps().get("name")));
                    fields.add(fieldToAdd);
                }
            }
        }
        return fields;
    }

    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values, Direction direction) {

        values.put("DIRECTION", direction.getValue());

        if(Direction.FORWARD == direction){
            values.put("valid", "true");
            caseworkClient.updateCase(caseUUID, stageUUID, values);
        }else{
            values.put("valid", "false");
        }

        camundaClient.completeTask(stageUUID, values);
    }
}
