package uk.gov.digital.ho.hocs.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.UpdateCaseworkCaseDataRequest;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CREATE_CASE_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;

@Slf4j
@Component
public class MigrationCaseworkClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;
    private RequestData requestData;

    @Autowired
    public MigrationCaseworkClient(RestHelper restHelper,
                                   @Value("${hocs.case-service}") String caseService,
                                   RequestData requestData) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseService;
        this.requestData = requestData;
    }

    public CreateCaseworkCaseResponse createCase(String caseDataType, String caseReference, Map<String, String> data, LocalDate dateReceived, LocalDate deadline) {
        MigrationCreateCaseworkCaseRequest request = new MigrationCreateCaseworkCaseRequest(caseDataType, caseReference, data, dateReceived, deadline);
        CreateCaseworkCaseResponse response = restHelper.post(serviceBaseURL, "/migration/case", request, CreateCaseworkCaseResponse.class);
        log.info("Created Case {}, {}", response.getUuid(), response.getReference(), value(EVENT, CREATE_CASE_SUCCESS));
        return response;
    }

    public UUID getStageUUID(UUID caseUUID) {
        log.info(serviceBaseURL + String.format("/migration/case/%s", caseUUID));
        UUID stageUUID = restHelper.get(serviceBaseURL, String.format("/migration/case/%s", caseUUID), UUID.class);
        return stageUUID;
    }


    public UUID saveCorrespondent(UUID caseUUID, UUID stageUUID, MigrationCreateCaseworkCorrespondentRequest correspondent) {
        UUID correspondentUUID = restHelper.post(serviceBaseURL, String.format("/migration/case/%s/stage/%s/correspondent", caseUUID, stageUUID), correspondent, UUID.class);
        return correspondentUUID;
    }


    public void updateCase(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        restHelper.put(serviceBaseURL, String.format("/migration/case/%s/stage/%s/data", caseUUID, stageUUID) , request, Void.class);
        log.info("Update Data for Case {}", caseUUID);
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondent) {
        String response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryCorrespondent", caseUUID, stageUUID), primaryCorrespondent, String.class);
        log.info("Update Correspondent for Case {}", caseUUID);
    }

    public UUID addTopic(UUID caseUUID, UUID stageUUID, UUID topic) {
        MigrationCreateCaseworkTopicRequest request = new MigrationCreateCaseworkTopicRequest(topic);
        UUID response = restHelper.post(serviceBaseURL, String.format("/migration/case/%s/stage/%s/topic", caseUUID, stageUUID), request, UUID.class);
        log.info("Add Topic for Case {}", caseUUID);
        return response;
    }

    public void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopic) {
        String response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryTopic", caseUUID, stageUUID), primaryTopic, String.class);
        log.info("Update Primary Topic for Case {}", caseUUID);
    }

    public void assignToMe(UUID caseUUID, UUID stageUUID) {
        MigrationUpdateStageUserRequest request = new MigrationUpdateStageUserRequest(UUID.fromString(requestData.userId()));
        String response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), request, String.class);
        log.info("Assign Case to User {}", caseUUID);
    }
}

