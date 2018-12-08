package uk.gov.digital.ho.hocs.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.Deadline;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoFormClient;
import uk.gov.digital.ho.hocs.workflow.api.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.domain.model.*;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class WorkflowService {

    private final CaseworkClient caseworkClient;
    private final DocumentClient documentClient;
    private final InfoClient infoClient;
    private final CamundaClient camundaClient;
    private final InfoFormClient infoFormClient;


    @Autowired
    public WorkflowService(CaseworkClient caseworkClient,
                           DocumentClient documentClient,
                           InfoClient infoClient,
                           CamundaClient camundaClient,
                           InfoFormClient infoFormClient) {
        this.caseworkClient = caseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
        this.infoFormClient = infoFormClient;
    }

    CreateCaseResponse createCase(CaseDataType caseDataType, LocalDate dateReceived, List<DocumentSummary> documents) {
        // Create a case in the casework service in order to get a reference back to display to the user.
        Map<String, String> data = new HashMap<>();
        data.put("DateReceived", dateReceived.toString());
        Deadline deadline = infoClient.getCaseDeadline(caseDataType, dateReceived);
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseDataType, data, dateReceived, deadline.getDate());
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {

            // Add Documents to the case
            createDocument(caseUUID, documents);

            // Start a new camunda workflow (caseUUID is the business key).
            Map<String, String> seedData = new HashMap<>();
            seedData.put("CaseReference",caseResponse.getReference());
            seedData.putAll(data);
            seedData.putAll(tempUserTeamCode());
            camundaClient.startCase(caseUUID, caseDataType, seedData);

        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
        return new CreateCaseResponse(caseUUID, caseResponse.getReference());
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

    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getStageScreenName(stageUUID);

        if(!screenName.equals("FINISH")) {

            GetCaseworkCaseDataResponse inputResponse = caseworkClient.getCase(caseUUID);

            HocsForm form = infoFormClient.getForm(screenName);
            form.setData(inputResponse.getData());
            return new GetStageResponse(stageUUID, inputResponse.getReference(), form);
        } else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values) {
        // TODO: validate Form
        values.put("valid", "true");
        camundaClient.completeTask(stageUUID, values);
        caseworkClient.updateCase(caseUUID, values);

        return getStage(caseUUID, stageUUID);
    }

    void createCorrespondent(UUID caseUUID, CorrespondentType type, String fullName, String postcode, String addressOne, String addressTwo, String addressThree, String addressCountry, String phone, String email, String reference ){
        Correspondent correspondent = new Correspondent(type, fullName, postcode, addressOne, addressTwo, addressThree, addressCountry, phone, email, reference);
        caseworkClient.createCorrespondent(caseUUID, correspondent);

    }

    void createTopic(UUID caseUUID, UUID topicUUID) {
        Topic topic = infoClient.getTopic(topicUUID);
        caseworkClient.addTopicToCase(caseUUID, topic.getValue(), topic.getLabel());
    }

    private static Map<String,String> tempUserTeamCode() {

        Map<String, String> seedData = new HashMap<>();
        seedData.put("DataInputTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("DataInputQATeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("MarkupTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("TransferConfirmationTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("NoReplyNeededTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("InitialDraftTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("QAResponseTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("PrivateOfficeTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("MinisterSignOffTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("DispatchTeamUUID", "44444444-2222-2222-2222-222222222222");
        seedData.put("CopyNumberTenTeamUUID", "44444444-2222-2222-2222-222222222222");

        return seedData;
    }
}
