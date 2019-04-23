package uk.gov.digital.ho.hocs.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

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


    @Autowired
    public MigrationWorkflowService(MigrationCaseworkClient migrationCaseworkClient,
                                    DocumentClient documentClient,
                                    InfoClient infoClient,
                                    CamundaClient camundaClient) {
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.camundaClient = camundaClient;
    }

    MigrationCreateCaseResponse createCase(String caseDataType, String caseReference, LocalDate dateReceived, LocalDate caseDeadline, Map<String, String> data, UUID topicUUID) {
        // Create a case in the casework service in order to get a reference back to display to the user.
        data.put("DateReceived", dateReceived.toString());
        CreateCaseworkCaseResponse caseResponse = migrationCaseworkClient.createCase(caseDataType, caseReference, data, dateReceived, caseDeadline);
        UUID caseUUID = caseResponse.getUuid();
        Map<String, String> seedData;
        if (caseUUID != null) {

            // Start a new camunda workflow (caseUUID is the business key).
            seedData = new HashMap<>();
            seedData.put("CaseReference", caseResponse.getReference());
            seedData.put("Topics", String.valueOf(topicUUID));
            seedData.put("CopyNumberTen", String.valueOf(data.get("CopyNumberTen")).toUpperCase());
            seedData.putAll(data);
            camundaClient.startCase(caseUUID, caseDataType, seedData);
            log.info("Camunda Start");

        } else {
            log.error("Failed to start case, invalid caseUUID!", value(EVENT, CASE_STARTED_FAILURE));
            throw new ApplicationExceptions.EntityCreationException("Failed to start case, invalid caseUUID!", CASE_STARTED_FAILURE);
        }
        return new MigrationCreateCaseResponse(caseUUID, caseResponse.getReference(), seedData);
    }

    MigrationProgressCaseResponse progressCase(UUID caseUUID, String caseDataType, Map<String, String> data, Map<String, String> seedData, List<MigrationCorrespondent> correspondents, UUID draftDocumentUUID, UUID topicUUID) {
        MigrationProgressCaseResponse migrationProgressCaseResponse = new MigrationProgressCaseResponse();
        UUID stageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
        UUID primaryCorrespondentUUID = null;

        if (correspondents != null) {
            for (MigrationCorrespondent correspondent : correspondents) {
                if (correspondent.getType() != null) {
                    correspondent.setUuid(saveCorrespondent(caseUUID, stageUUID, correspondent));
                    if (correspondent.getIsPrimary() == true) {
                        primaryCorrespondentUUID = correspondent.getUuid();
                    }
                }
            }
        }
        if (primaryCorrespondentUUID != null) {
            migrationCaseworkClient.updatePrimaryCorrespondent(caseUUID, stageUUID, primaryCorrespondentUUID);
        }
        UUID returnedTopicUUID = null;
        if (topicUUID != null) {
            returnedTopicUUID = migrationCaseworkClient.addTopic(caseUUID, stageUUID, topicUUID);
            migrationCaseworkClient.updatePrimaryTopic(caseUUID, stageUUID, returnedTopicUUID);
        }
// Set Response Channel to Letter if null
        if (data.get("ResponseChannel") == null) {
            data.replace("ResponseChannel", "LETTER");
        }


        if (data.get("caseTask").equals("Create case")) {
            /* Complete nothing */
            migrationProgressCaseResponse.setStage("data input");
        }
        if (data.get("caseTask").equals("Mark up") || data.get("caseTask").equals("QA case")) {
            /* Complete data input*/
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            migrationProgressCaseResponse.setStage("Mark up");
        }
        if (data.get("caseTask").equals("QA")) {
            /*start nrn*/
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            startNRNMarkup(data, caseUUID);
            migrationProgressCaseResponse.setStage("NRN");
        }
        if (data.get("caseTask").equals("Transfer")) {
            /* start OGD*/
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            startOGDMarkup(data, caseUUID);
            migrationProgressCaseResponse.setStage("OGD");
        }
        if (data.get("caseTask").equals("Amend response") || data.get("caseTask").equals("Draft response")) {
            /* Complete markUp*/
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            completeMarkup(data, caseUUID, returnedTopicUUID);
            migrationProgressCaseResponse.setStage("Initial Draft");
        }
        if (data.get("caseTask").equals("QA review")) {
            /* Complete initial draft*/
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            completeMarkup(data, caseUUID, returnedTopicUUID);
            completeInitialDraft(data, caseUUID, draftDocumentUUID, caseDataType);
            migrationProgressCaseResponse.setStage("QA Response");
        }
        if (data.get("caseTask").equals("HS Private Office approval") || data.get("caseTask").equals("Private Office approval")) {
            /* Complete QA response */
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            completeMarkup(data, caseUUID, returnedTopicUUID);
            completeInitialDraft(data, caseUUID, draftDocumentUUID, caseDataType);
            completeQAResponse(caseUUID);
            migrationProgressCaseResponse.setStage("Private Office");
        }
        if (data.get("caseTask").equals("Home Sec's sign-off") || data.get("caseTask").equals("Minister's sign-off")) {
            /* Complete Private Office */
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            completeMarkup(data, caseUUID, returnedTopicUUID);
            completeInitialDraft(data, caseUUID, draftDocumentUUID, caseDataType);
            completeQAResponse(caseUUID);
            completePrivateOfficeSignoff(caseUUID, caseDataType);
            migrationProgressCaseResponse.setStage("Minister Sign Off");
        }
        if (data.get("caseTask").equals("Dispatch response")) {
            /* Complete Minister sign off*/
            completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
            completeMarkup(data, caseUUID, returnedTopicUUID);
            completeInitialDraft(data, caseUUID, draftDocumentUUID, caseDataType);
            completeQAResponse(caseUUID);
            completePrivateOfficeSignoff(caseUUID, caseDataType);
            completeMinisterSignOff(caseUUID, caseDataType);
            migrationProgressCaseResponse.setStage("Dispatch");
        }
        if (data.get("caseTask").equals("None")) {
            /* Complete Dispatch*/
            if (data.get("MarkupDecision").equals("PR") || data.get("MarkupDecision").equals("FAQ")) {
                completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
                completeMarkup(data, caseUUID, returnedTopicUUID);
                completeInitialDraft(data, caseUUID, draftDocumentUUID, caseDataType);
                completeQAResponse(caseUUID);
                completePrivateOfficeSignoff(caseUUID, caseDataType);
                completeMinisterSignOff(caseUUID, caseDataType);
                completeDispatch(data, caseUUID, caseDataType);
            } else if (data.get("MarkupDecision").equals("NRN")) {
                completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
                startNRNMarkup(data, caseUUID);
                completeNRNConfirmation(data, caseUUID);
            } else if (data.get("MarkupDecision").equals("OGD")) {
                completeDataInput(data, caseUUID, stageUUID, primaryCorrespondentUUID, seedData);
                startOGDMarkup(data, caseUUID);
                completeOGDConfirmation(data, caseUUID);
            }
            migrationProgressCaseResponse.setStage("Completed");
        }

        return migrationProgressCaseResponse;
    }

    UUID saveCorrespondent(UUID caseUUID, UUID stageUUID, MigrationCorrespondent correspondent) {
        String fullname;
        if (correspondent.getFullname() == null || correspondent.getFullname().isEmpty()) {
            fullname = correspondent.getTitle() + " " + correspondent.getForename() + " " + correspondent.getSurname();
        } else {
            fullname = correspondent.getFullname();
        }

        MigrationCreateCaseworkCorrespondentRequest correspondentRequest = new MigrationCreateCaseworkCorrespondentRequest(
                correspondent.getType(),
                fullname,
                correspondent.getPostcode(),
                correspondent.getAddress1(),
                correspondent.getAddress2(),
                correspondent.getAddress3(),
                correspondent.getCountry(),
                correspondent.getTelephone(),
                correspondent.getEmail(),
                correspondent.getReference()
        );

        return migrationCaseworkClient.saveCorrespondent(caseUUID, stageUUID, correspondentRequest);
    }

    void createDocument(UUID caseUUID, List<DocumentSummary> documents) {
        if (documents != null) {
            // Add any Documents to the case
            for (DocumentSummary document : documents) {
                documentClient.createDocument(caseUUID, document);
            }
        }
    }

    private void completeDataInput(Map<String, String> data, UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID, Map<String, String> seedData) {
        Map<String, String> dataInputData = new HashMap<>();
        dataInputData.put("DateOfCorrespondence", String.valueOf(data.get("DateOfCorrespondence")));
        dataInputData.put("DateReceived", String.valueOf(data.get("DateReceived")));
        dataInputData.put("OriginalChannel", String.valueOf(data.get("OriginalChannel")));
        dataInputData.put("CopyNumberTen", String.valueOf(data.get("CopyNumberTen")).toUpperCase());
        migrationCaseworkClient.updateCase(caseUUID, stageUUID, dataInputData);
        camundaClient.completeTask(stageUUID, dataInputData);
        Map<String, String> correspondentData = new HashMap<>();
        correspondentData.put("Correspondents", String.valueOf(primaryCorrespondentUUID));
        migrationCaseworkClient.updateCase(caseUUID, stageUUID, correspondentData);
        camundaClient.completeTask(stageUUID, correspondentData);
    }

    private void completeMarkup(Map<String, String> data, UUID caseUUID, UUID returnedTopicUUID) {
        UUID markUpStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);

        migrationCaseworkClient.assignToMe(caseUUID, markUpStageUUID);
        Map<String, String> markupData = new HashMap<>();
        markupData.put("MarkupDecision", String.valueOf(data.get("MarkupDecision")));
        migrationCaseworkClient.updateCase(caseUUID, markUpStageUUID, markupData);
        camundaClient.completeTask(markUpStageUUID, markupData);
        Map<String, String> topicData = new HashMap<>();
        topicData.put("Topics", String.valueOf(returnedTopicUUID));
        migrationCaseworkClient.updateCase(caseUUID, markUpStageUUID, topicData);
        camundaClient.completeTask(markUpStageUUID, topicData);
        Map<String, String> teamsForTopic = new HashMap<>();
        TeamDto draftingTeam = infoClient.getTeamForTopicAndStage(caseUUID, returnedTopicUUID, "DCU_MIN_INITIAL_DRAFT");
        TeamDto pOTeam = infoClient.getTeamForTopicAndStage(caseUUID, returnedTopicUUID, "DCU_MIN_PRIVATE_OFFICE");
        teamsForTopic.put("DraftingTeamUUID", draftingTeam.getUuid().toString());
        teamsForTopic.put("DraftingTeamName", draftingTeam.getDisplayName());
        teamsForTopic.put("POTeamUUID", pOTeam.getUuid().toString());
        teamsForTopic.put("POTeamName", pOTeam.getDisplayName());
        migrationCaseworkClient.updateCase(caseUUID, markUpStageUUID, teamsForTopic);
        camundaClient.completeTask(markUpStageUUID, teamsForTopic);
    }

    private void startNRNMarkup(Map<String, String> data, UUID caseUUID) {
        UUID markUpStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);

        migrationCaseworkClient.assignToMe(caseUUID, markUpStageUUID);
        Map<String, String> markupData = new HashMap<>();
        markupData.put("MarkupDecision", String.valueOf(data.get("MarkupDecision")));
        migrationCaseworkClient.updateCase(caseUUID, markUpStageUUID, markupData);
        camundaClient.completeTask(markUpStageUUID, markupData);
        Map<String, String> nrnData = new HashMap<>();
        nrnData.put("CaseNote_NRN", "migration case, see case notes");
        migrationCaseworkClient.updateCase(caseUUID, markUpStageUUID, nrnData);
        camundaClient.completeTask(markUpStageUUID, nrnData);
    }

    private void completeNRNConfirmation(Map<String, String> data, UUID caseUUID) {
        UUID nrnConfirmationStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);

        migrationCaseworkClient.assignToMe(caseUUID, nrnConfirmationStageUUID);
        Map<String, String> nrnConfirmationData = new HashMap<>();
        nrnConfirmationData.put("CaseNote_NRN", "see case notes");
        nrnConfirmationData.put("NoReplyNeededConfirmation", "ACCEPT");
        migrationCaseworkClient.updateCase(caseUUID, nrnConfirmationStageUUID, nrnConfirmationData);
        camundaClient.completeTask(nrnConfirmationStageUUID, nrnConfirmationData);
    }

    private void startOGDMarkup(Map<String, String> data, UUID caseUUID) {
        UUID ogdmarkUpStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);

        migrationCaseworkClient.assignToMe(caseUUID, ogdmarkUpStageUUID);
        Map<String, String> markupData = new HashMap<>();
        markupData.put("MarkupDecision", String.valueOf(data.get("MarkupDecision")));
        migrationCaseworkClient.updateCase(caseUUID, ogdmarkUpStageUUID, markupData);
        camundaClient.completeTask(ogdmarkUpStageUUID, markupData);
        Map<String, String> ogdData = new HashMap<>();
        ogdData.put("OGDDept", data.get("OGDName"));
        ogdData.put("CaseNote_OGD", "migration case, see case notes");
        migrationCaseworkClient.updateCase(caseUUID, ogdmarkUpStageUUID, ogdData);
        camundaClient.completeTask(ogdmarkUpStageUUID, ogdData);
    }

    private void completeOGDConfirmation(Map<String, String> data, UUID caseUUID) {
        UUID ogdConfirmationStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);

        migrationCaseworkClient.assignToMe(caseUUID, ogdConfirmationStageUUID);
        Map<String, String> ogdConfirmationData = new HashMap<>();
        ogdConfirmationData.put("OGDDept", data.get("OGDName"));
        ogdConfirmationData.put("CaseNote_OGD", "");
        ogdConfirmationData.put("TransferConfirmation", "ACCEPT");
        migrationCaseworkClient.updateCase(caseUUID, ogdConfirmationStageUUID, ogdConfirmationData);
        camundaClient.completeTask(ogdConfirmationStageUUID, ogdConfirmationData);
    }

    private void completeInitialDraft(Map<String, String> data, UUID caseUUID, UUID documentUUID, String caseDataType) {
        UUID initialDraftStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
        migrationCaseworkClient.assignToMe(caseUUID, initialDraftStageUUID);
        Map<String, String> teamAnswerData = new HashMap<>();
        teamAnswerData.put("InitialDraftDecision", "ACCEPT");
        migrationCaseworkClient.updateCase(caseUUID, initialDraftStageUUID, teamAnswerData);
        camundaClient.completeTask(initialDraftStageUUID, teamAnswerData);
        if (caseDataType.equals("MIN") || caseDataType.equals("TRO")) {
            Map<String, String> responseData = new HashMap<>();
            responseData.put("ResponseChannel", data.get("ResponseChannel"));
            migrationCaseworkClient.updateCase(caseUUID, initialDraftStageUUID, responseData);
            camundaClient.completeTask(initialDraftStageUUID, responseData);
        }
        Map<String, String> draftDocumentData = new HashMap<>();
        draftDocumentData.put("DraftDocuments", String.valueOf(documentUUID));
        migrationCaseworkClient.updateCase(caseUUID, initialDraftStageUUID, draftDocumentData);
        camundaClient.completeTask(initialDraftStageUUID, draftDocumentData);
        if (caseDataType.equals("MIN") || caseDataType.equals("DTEN")) {
            Map<String, String> offlineQAData = new HashMap<>();
            offlineQAData.put("OfflineQA", "FALSE");
            migrationCaseworkClient.updateCase(caseUUID, initialDraftStageUUID, offlineQAData);
            camundaClient.completeTask(initialDraftStageUUID, offlineQAData);
        }
    }

    private void completeQAResponse(UUID caseUUID) {
        UUID qaResponseStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
        migrationCaseworkClient.assignToMe(caseUUID, qaResponseStageUUID);
        Map<String, String> qaResponseData = new HashMap<>();
        qaResponseData.put("QAResponseDecision", "ACCEPT");
        migrationCaseworkClient.updateCase(caseUUID, qaResponseStageUUID, qaResponseData);
        camundaClient.completeTask(qaResponseStageUUID, qaResponseData);
    }

    private void completePrivateOfficeSignoff(UUID caseUUID, String caseDataType) {
        if (caseDataType.equals("MIN") || caseDataType.equals("DTEN")) {
            UUID privateOfficeSignOffStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
            migrationCaseworkClient.assignToMe(caseUUID, privateOfficeSignOffStageUUID);
            Map<String, String> privateOfficeResponseData = new HashMap<>();
            privateOfficeResponseData.put("PrivateOfficeDecision", "ACCEPT");
            migrationCaseworkClient.updateCase(caseUUID, privateOfficeSignOffStageUUID, privateOfficeResponseData);
            camundaClient.completeTask(privateOfficeSignOffStageUUID, privateOfficeResponseData);
        }
    }

    private void completeMinisterSignOff(UUID caseUUID, String caseDataType) {
        if (caseDataType.equals("MIN")) {
            UUID ministerSignOffStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
            migrationCaseworkClient.assignToMe(caseUUID, ministerSignOffStageUUID);
            Map<String, String> ministerResponseData = new HashMap<>();
            ministerResponseData.put("MinisterSignOffDecision", "ACCEPT");
            migrationCaseworkClient.updateCase(caseUUID, ministerSignOffStageUUID, ministerResponseData);
            camundaClient.completeTask(ministerSignOffStageUUID, ministerResponseData);
        }
    }

    private void completeDispatch(Map<String, String> data, UUID caseUUID, String caseDataType) {
        UUID dispatchStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
        migrationCaseworkClient.assignToMe(caseUUID, dispatchStageUUID);
        Map<String, String> dispatchData = new HashMap<>();
        dispatchData.put("DispatchDecision", "ACCEPT");
        dispatchData.put("ResponseChannel", data.get("ResponseChannel"));
        dispatchData.put("valid", "true");
        migrationCaseworkClient.updateCase(caseUUID, dispatchStageUUID, dispatchData);
        camundaClient.completeTask(dispatchStageUUID, dispatchData);
        if (data.get("CopyNumberTen").equals("true") && ((caseDataType.equals("MIN")) || (caseDataType.equals("TRO")))) {
            completeCopyNumberTen(caseUUID);
        }
    }

    private void completeCopyNumberTen(UUID caseUUID) {
        UUID copyToNoTenStageUUID = migrationCaseworkClient.getStageUUID(caseUUID);
        migrationCaseworkClient.assignToMe(caseUUID, copyToNoTenStageUUID);
        Map<String, String> copyToNoTenData = new HashMap<>();
        migrationCaseworkClient.updateCase(caseUUID, copyToNoTenStageUUID, copyToNoTenData);
        camundaClient.completeTask(copyToNoTenStageUUID, copyToNoTenData);
    }
}
