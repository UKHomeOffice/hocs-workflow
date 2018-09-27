package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.*;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkCaseTypeResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkInputResponse;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkStageResponse;
import uk.gov.digital.ho.hocs.workflow.documentClient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.model.*;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class WorkflowService {

    private final CaseworkClient caseworkClient;
    private final DocumentClient documentClient;
    private final InfoClient infoClient;
    private final CamundaClient camundaClient;
    private final HocsFormService hocsFormService;


    @Autowired
    public WorkflowService(CaseworkClient caseworkClient,
                           DocumentClient documentClient,
                           InfoClient infoClient,
                           CamundaClient camundaClient,
                           HocsFormService hocsFormService) {
        this.caseworkClient = caseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
        this.hocsFormService = hocsFormService;
    }

    CreateCaseResponse createCase(CaseType caseType, LocalDate dateReceived, List<DocumentSummary> documents) {
        // Create a case in the casework service in order to get a UUID.
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseType);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {
            // Start a new case level workflow (caseUUID is the business key).
            Map<String, Object> seedData = new HashMap<>();
            seedData.put("DateReceived", dateReceived);

            seedData.put("DataInputTeamUUID", "44444444-2222-2222-2222-222222222222");
            seedData.put("DataInputQATeamUUID", "22222222-2222-2222-2222-222222222222");
            seedData.put("MarkupTeamUUID", "11111111-1111-1111-1111-111111111111");
            seedData.put("TransferConfirmationTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("NoReplyNeededTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("InitialDraftTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("QAResponseTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("PrivateOfficeTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("MinisterSignOffTeamUUID", "33333333-3333-3333-3333-333333333333");
            seedData.put("DispatchTeamUUID", "33333333-3333-3333-3333-333333333333");

            Map<String, String> data = new HashMap<>();
            data.put("DateReceived", dateReceived.toString());
            caseworkClient.setInputData(caseUUID, data);
            createDocument(caseUUID, documents);
            camundaClient.startCase(caseUUID, caseType, seedData);

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

    void deleteDocument(UUID caseUUID, UUID documentUUID) {
         documentClient.deleteDocument(caseUUID, documentUUID);
    }

    GetCorrespondentResponse getCorrespondentData(UUID caseUUID, UUID correspondentUUID) {
        GetCorrespondentResponse correspondent = caseworkClient.getCorrespondentForCase(caseUUID, correspondentUUID);
        return correspondent;
    }

    void addCorrespondentToCase(UUID caseUUID, CorrespondentType type, String fullName, String postcode, String addressOne, String addressTwo, String addressThree, String addressCountry, String phone, String email, String reference ){
        Correspondent correspondent = new Correspondent(type, fullName, postcode, addressOne, addressTwo, addressThree, addressCountry, phone, email);
        caseworkClient.createCorrespondent(caseUUID, correspondent);

        if(reference != null) {
            caseworkClient.createReference(caseUUID, ReferenceType.CORESPONDENT_REFERENCE, reference);
        }
    }

    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getScreenName(stageUUID);
        HocsForm form = hocsFormService.getForm(screenName);

        // If the stage is complete we have form as null.
        if (form != null) {
            // TODO: permission check (active stage userID? TeamID ?)
            GetCaseworkStageResponse stageResponse = caseworkClient.getStage(caseUUID, stageUUID);
            GetCaseworkInputResponse inputResponse = caseworkClient.getInput(caseUUID);
            form.setData(inputResponse.getData());
            return new GetStageResponse(stageUUID, stageResponse.getCaseReference(), form);
        } else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values) {
        // TODO: permission check (active stage userID? TeamID ?)
        // TODO: validate Form
        caseworkClient.setInputData(caseUUID, values);
        camundaClient.updateStage(stageUUID, values);

        return getStage(caseUUID, stageUUID);
    }

    void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        camundaClient.allocateStage(caseUUID, teamUUID, userUUID);
        caseworkClient.allocateStage(caseUUID, stageUUID, teamUUID, userUUID);
    }

    GetParentTopicResponse getParentTopics(UUID caseUUID) {
        GetCaseworkCaseTypeResponse caseTypeResponse = caseworkClient.getCaseTypeForCase(caseUUID);
        return infoClient.getParentTopics(caseTypeResponse.getType().toString());
    }

    GetCaseTopicsResponse getCaseTopics(UUID caseUUID) {
        GetCaseTopicsResponse caseTopicsResponse = caseworkClient.getCaseTopics(caseUUID);
        return caseTopicsResponse;
    }

    Topic getTopicData(UUID topicUUID) {
        Topic topic = infoClient.getTopic(topicUUID);
        return topic;
    }

    void addTopicToCase(UUID caseUUID, UUID topicUUID) {
        Topic topic = infoClient.getTopic(topicUUID);
        caseworkClient.addTopicToCase(caseUUID, topic.getValue(), topic.getLabel());
    }

    void deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        caseworkClient.deleteTopicFromCase(caseUUID, topicUUID);
    }
}
