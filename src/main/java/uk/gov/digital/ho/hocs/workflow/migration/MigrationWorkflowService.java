package uk.gov.digital.ho.hocs.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoFormClient;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CASE_STARTED_FAILURE;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;

@Service
@Slf4j
public class MigrationWorkflowService {

    private final MigrationCaseworkClient migrationCaseworkClient;
    private final DocumentClient documentClient;
    private final InfoClient infoClient;
    private final CamundaClient camundaClient;
    private final InfoFormClient infoFormClient;


    @Autowired
    public MigrationWorkflowService(MigrationCaseworkClient migrationCaseworkClient,
                                    DocumentClient documentClient,
                                    InfoClient infoClient,
                                    CamundaClient camundaClient,
                                    InfoFormClient infoFormClient) {
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
        this.infoFormClient = infoFormClient;
    }

    CreateCaseResponse createCase(CaseDataType caseDataType, String caseReference, LocalDate dateReceived, LocalDate caseDeadline, Map<String, String> data, List<DocumentSummary> documents, List<MigrationCorrespondent> correspondents, UUID topicUUID) {
        // Create a case in the casework service in order to get a reference back to display to the user.
        data.put("DateReceived", dateReceived.toString());
        CreateCaseworkCaseResponse caseResponse = migrationCaseworkClient.createCase(caseDataType, caseReference, data, dateReceived, caseDeadline);
        UUID caseUUID = caseResponse.getUuid();
        UUID stageUUID;
        UUID correspondentUUID = null;
        if (caseUUID != null) {

            // Add Documents to the case
            createDocument(caseUUID, documents);

            // Start a new camunda workflow (caseUUID is the business key).
            Map<String, String> seedData = new HashMap<>();
            seedData.put("CaseReference", caseResponse.getReference());
            seedData.putAll(data);
            camundaClient.startCase(caseUUID, caseDataType, seedData);
            log.info("Camunda Start");
            stageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
            for (MigrationCorrespondent correspondent : correspondents) {
                correspondent.setUuid(saveCorrespondent(caseUUID, stageUUID, correspondent));
                if(correspondent.getIsPrimary() == true){
                    correspondentUUID = correspondent.getUuid();
                }
            }



            migrationCaseworkClient.updatePrimaryCorrespondent(caseUUID, stageUUID, correspondentUUID);
            UUID returnedTopicUUID = migrationCaseworkClient.addTopic(caseUUID, stageUUID, topicUUID);
            migrationCaseworkClient.updatePrimaryTopic(caseUUID, stageUUID, returnedTopicUUID);

//            camundaClient.completeTask(stageUUID, seedData);
//            log.info("Camunda complete task");


        } else {
            log.error("Failed to start case, invalid caseUUID!", value(EVENT, CASE_STARTED_FAILURE));
            throw new ApplicationExceptions.EntityCreationException("Failed to start case, invalid caseUUID!", CASE_STARTED_FAILURE);
        }
        return new CreateCaseResponse(caseUUID, caseResponse.getReference());
    }

    UUID saveCorrespondent(UUID caseUUID, UUID stageUUID, MigrationCorrespondent correspondent) {


        MigrationCreateCaseworkCorrespondentRequest correspondentRequest = new MigrationCreateCaseworkCorrespondentRequest(
                correspondent.getType(),
                correspondent.getTitle() + " " + correspondent.getForename() + " " + correspondent.getSurname(),
                correspondent.getPostcode(),
                correspondent.getAddress1(),
                correspondent.getAddress2(),
                correspondent.getAddress3(),
                correspondent.getCountry(),
                correspondent.getTelephone(),
                correspondent.getEmail(),
                correspondent.getReference()
        );

        UUID correspondentUUID = migrationCaseworkClient.saveCorrespondent(caseUUID, stageUUID, correspondentRequest);

        return correspondentUUID;
    }

    void createDocument(UUID caseUUID, List<DocumentSummary> documents) {
        if (documents != null) {
            // Add any Documents to the case
            for (DocumentSummary document : documents) {
                UUID response = documentClient.createDocument(caseUUID, document.getDisplayName(), document.getType());

                documentClient.processDocument(response, document.getS3UntrustedUrl());
            }
        }
    }


}
