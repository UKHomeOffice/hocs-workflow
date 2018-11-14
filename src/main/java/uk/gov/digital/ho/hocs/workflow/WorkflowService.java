package uk.gov.digital.ho.hocs.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.camundaClient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.*;
import uk.gov.digital.ho.hocs.workflow.documentClient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoGetStandardLineResponse;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoGetTemplateResponse;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.model.Correspondent;
import uk.gov.digital.ho.hocs.workflow.model.CorrespondentType;
import uk.gov.digital.ho.hocs.workflow.model.ReferenceType;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;

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

    CreateCaseResponse createCase(CaseDataType caseDataType, LocalDate dateReceived, List<DocumentSummary> documents) {
        // Create a case in the casework service in order to get a UUID.
        Map<String, String> data = new HashMap<>();
        data.put("DateReceived", dateReceived.toString());
        CreateCaseworkCaseResponse caseResponse = caseworkClient.createCase(caseDataType);
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

    GetParentTopicResponse getParentTopicsAndTopics(UUID caseUUID) {
        GetCaseworkCaseTypeResponse caseTypeResponse = caseworkClient.getCaseTypeForCase(caseUUID);
        return infoClient.getParentTopicsAndTopics(caseTypeResponse.getType().toString());
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

    public void deleteCorrespondentFromCase(UUID caseUUID, UUID correspondentUUID) {
        caseworkClient.deleteCorrespondentFromCase(caseUUID,correspondentUUID);
    }

    public InfoGetTemplateResponse getTemplates(UUID caseUUID) {
        GetCaseworkCaseTypeResponse caseTypeResponse = caseworkClient.getCaseTypeForCase(caseUUID);
        return infoClient.getTemplate(caseTypeResponse.getType());
    }

    public InfoGetStandardLineResponse getStandardLines(UUID caseUUID) {
        GetPrimaryTopicResponse getPrimaryTopicResponse = caseworkClient.getCaseTypeAndTopicForCase(caseUUID);
        return infoClient.getStandardLine(getPrimaryTopicResponse.getTopicUUID());
    }

    private static Map<String,Object> tempUserTeamCode() {

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
