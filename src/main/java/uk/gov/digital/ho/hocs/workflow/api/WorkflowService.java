package uk.gov.digital.ho.hocs.workflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsCaseSchema;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;

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

    @Autowired
    public WorkflowService(CaseworkClient caseworkClient,
                           DocumentClient documentClient,
                           InfoClient infoClient,
                           CamundaClient camundaClient, ObjectMapper objectMapper) {
        this.caseworkClient = caseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
    }

    public CreateCaseResponse createCase(CaseDataType caseDataType, LocalDate dateReceived, List<DocumentSummary> documents) {
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
                UUID response = documentClient.createDocument(caseUUID, document.getDisplayName(), document.getType());

                documentClient.processDocument(response, document.getS3UntrustedUrl());
            }
        }
    }

    public GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getStageScreenName(stageUUID);

        if(!screenName.equals("FINISH")) {

            GetCaseworkCaseDataResponse inputResponse = caseworkClient.getCase(caseUUID);

            SchemaDto schemaDto = infoClient.getSchema(screenName);
            List<HocsFormField> fields = schemaDto.getFields().stream().map(HocsFormField::from).collect(Collectors.toList());
            HocsSchema schema = new HocsSchema(schemaDto.getTitle(), schemaDto.getDefaultActionLabel(), fields);
            HocsForm form = new HocsForm(schema,inputResponse.getData());
            return new GetStageResponse(stageUUID, inputResponse.getReference(), form);
        } else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    public GetCaseResponse getAllCaseStages(UUID caseUUID) {

            GetCaseworkCaseDataResponse inputResponse = caseworkClient.getCase(caseUUID);

            Set<SchemaDto> schemaDtos = infoClient.getSchemasForCaseType(inputResponse.getType().getType());

            Map<String, List<SchemaDto>> stageSchemas = schemaDtos.stream().collect(Collectors.groupingBy(SchemaDto::getStageType));

            Map<String, List<HocsFormField>> hocsFields =  stageSchemas.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, c -> schemasToFormField(c.getValue())));

            HocsCaseSchema schema = new HocsCaseSchema("View Case", hocsFields);

            return new GetCaseResponse(inputResponse.getReference(), schema, inputResponse.getData());
    }

    private static List<HocsFormField> schemasToFormField(List<SchemaDto> schemaDtos) {
        List<HocsFormField> fields = new ArrayList<>();
        for(SchemaDto schemaDto : schemaDtos) {
            fields.add(HocsFormField.fromTitle(schemaDto.getTitle()));
            fields.addAll(schemaDto.getFields().stream().map(HocsFormField::from).collect(Collectors.toSet()));
        }
        return fields;
    }

    public GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values) {
        // TODO: validate Form
        values.put("valid", "true");
        caseworkClient.updateCase(caseUUID, stageUUID, values);
        camundaClient.completeTask(stageUUID, values);

        return getStage(caseUUID, stageUUID);
    }
}
