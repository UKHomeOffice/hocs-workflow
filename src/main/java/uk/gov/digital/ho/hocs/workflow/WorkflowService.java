package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.*;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.*;
import uk.gov.digital.ho.hocs.workflow.documentClient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoGetStandardLineListResponse;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoGetTemplateListResponse;
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

    CreateCaseResponse createCase(CaseDataType caseDataType, LocalDate dateReceived, Set<DocumentSummary> documents) {

        // Create a case in the casework service in order to get a UUID.
        Map<String, String> data = new HashMap<>();
        data.put("DateReceived", dateReceived.toString());
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseDataType, data);
        UUID caseUUID = caseResponse.getUuid();

        if (caseUUID != null) {

            // Add Documents to the case
            createDocument(caseUUID, documents);

            // Start a new camunda workflow (caseUUID is the business key).
            Map<String, Object> seedData = new HashMap<>();
            seedData.put("CaseReference",caseResponse.getReference());
            seedData.putAll(data);
            seedData.putAll(tempUserTeamCode());
            camundaClient.startCase(caseUUID, caseDataType, seedData);

        } else {
            throw new EntityCreationException("Failed to start case, invalid caseUUID!");
        }
        return new CreateCaseResponse(caseUUID, caseResponse.getReference());
    }

    void createDocument(UUID caseUUID, Set<DocumentSummary> documents) {
        if (documents.size() > 0) {
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
        GetCorrespondentResponse correspondent = caseworkClient.getCorrespondent(caseUUID, correspondentUUID);
        return correspondent;
    }

    void addCorrespondentToCase(UUID caseUUID, CorrespondentType type, String fullName, String postcode, String addressOne, String addressTwo, String addressThree, String addressCountry, String phone, String email, String reference ){
        Correspondent correspondent = new Correspondent(type, fullName, postcode, addressOne, addressTwo, addressThree, addressCountry, phone, email, reference);
        caseworkClient.createCorrespondent(caseUUID, correspondent);
    }

    GetStageResponse getStage(UUID caseUUID, UUID stageUUID) {
        String screenName = camundaClient.getScreenName(stageUUID);
        HocsForm form = hocsFormService.getForm(screenName);

        // If the stage is complete we have form as null.
        if (form != null) {
            GetCaseworkStageResponse stageResponse = caseworkClient.getStage(caseUUID, stageUUID);
            form.setData(stageResponse.getData());
            return new GetStageResponse(stageUUID, stageResponse.getCaseReference(), form);
        } else {
            return new GetStageResponse(stageUUID, null, null);
        }
    }

    GetStageResponse updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> values) {
        // TODO: permission check (active stage userID? TeamID ?)
        // TODO: validate Form
        caseworkClient.updateCase(caseUUID, values);


        camundaClient.updateStage(stageUUID, values);

        return getStage(caseUUID, stageUUID);
    }

    void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        camundaClient.allocateStage(caseUUID, teamUUID, userUUID);
        caseworkClient.updateStage(caseUUID, stageUUID, teamUUID, userUUID, StageStatusType.UPDATED);
    }

    GetParentTopicResponse getParentTopicsAndTopics(UUID caseUUID) {
        GetCaseworkCaseResponse caseTypeResponse = caseworkClient.getCaseworkCase(caseUUID);
        return infoClient.getParentTopicsAndTopics(caseTypeResponse.getType().toString());
    }

    GetCaseTopicsResponse getCaseTopics(UUID caseUUID) {
        GetCaseTopicsResponse caseTopicsResponse = caseworkClient.getTopics(caseUUID);
        return caseTopicsResponse;
    }

    Topic getTopicData(UUID topicUUID) {
        Topic topic = infoClient.getTopic(topicUUID);
        return topic;
    }

    void addTopicToCase(UUID caseUUID, UUID topicUUID) {
        Topic topic = infoClient.getTopic(topicUUID);
        caseworkClient.createTopic(caseUUID, topic.getValue(), topic.getLabel());
    }

    void deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        caseworkClient.deleteTopic(caseUUID, topicUUID);
    }

    void deleteCorrespondentFromCase(UUID caseUUID, UUID correspondentUUID) {
        caseworkClient.deleteCorrespondent(caseUUID,correspondentUUID);
    }

    InfoGetTemplateListResponse getTemplatesList(UUID caseUUID) {
        GetCaseworkCaseResponse caseworkCaseResponse = caseworkClient.getCaseworkCase(caseUUID);
        return infoClient.getTemplateList(caseworkCaseResponse.getType());
    }

    InfoGetStandardLineListResponse getStandardLineList(UUID caseUUID) {
        Topic topic = caseworkClient.getPrimaryTopic(caseUUID);
        UUID primaryTopic = topic.getValue();
        return infoClient.getStandardLineList(primaryTopic);

    }

    private Map<String,Object> tempUserTeamCode() {

        Map<String, Object> seedData = new HashMap<>();
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
        seedData.put("CopyNumberTenTeamUUID", "33333333-3333-3333-3333-333333333333");

        return seedData;
    }
}
