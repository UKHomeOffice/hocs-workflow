package uk.gov.digital.ho.hocs.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Slf4j
@Component
public class MigrationCaseworkClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public MigrationCaseworkClient(RestHelper restHelper,
                                   @Value("${hocs.case-service}") String caseService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = caseService;
    }

    public CreateCaseworkCaseResponse createCase(CaseDataType caseDataType, String caseReference, Map<String, String> data, LocalDate dateReceived, LocalDate deadline) {
        CaseType caseType = new CaseType(caseDataType.getType(), caseDataType.getShortCode(), caseDataType.getType());
        MigrationCreateCaseworkCaseRequest request = new MigrationCreateCaseworkCaseRequest(caseType, caseReference, data, dateReceived, deadline);
        ResponseEntity<CreateCaseworkCaseResponse> response = restHelper.post(serviceBaseURL, "/migration/case", request, CreateCaseworkCaseResponse.class);
        if (response.getStatusCodeValue() == 200) {
            log.info("Created Case {}, {}", response.getBody().getUuid(), response.getBody().getReference(), value(EVENT, CREATE_CASE_SUCCESS));
            return response.getBody();
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not create Case; response: %s ", response.getStatusCodeValue()), CREATE_CASE_FAILURE);
        }
    }

    public UUID getStageUUID (UUID caseUUID){
        log.info(serviceBaseURL+String.format("/migration/case/%s", caseUUID));
        ResponseEntity<UUID> stageUUID = restHelper.get(serviceBaseURL, String.format("/migration/case/%s", caseUUID), UUID.class);
        return stageUUID.getBody();
    }


    public UUID saveCorrespondent(UUID caseUUID, UUID stageUUID, MigrationCreateCaseworkCorrespondentRequest correspondent){
        ResponseEntity<UUID> correspondentUUID = restHelper.post(serviceBaseURL, String.format("/migration/case/%s/stage/%s/correspondent", caseUUID, stageUUID), correspondent, UUID.class);
        return correspondentUUID.getBody();
    }


    public void updateCase(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/data", caseUUID, stageUUID) , request, String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondent) {
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryCorrespondent", caseUUID, stageUUID) , primaryCorrespondent, String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
    }

    public UUID addTopic(UUID caseUUID, UUID stageUUID, UUID topic) {
        MigrationCreateCaseworkTopicRequest request = new MigrationCreateCaseworkTopicRequest(topic);
        ResponseEntity<UUID> response = restHelper.post(serviceBaseURL, String.format("/migration/case/%s/stage/%s/topic", caseUUID, stageUUID) , request, UUID.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
        return response.getBody();
    }

    public void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopic) {
        ResponseEntity<String> response = restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/primaryTopic", caseUUID, stageUUID) , primaryTopic, String.class);

        if (response.getStatusCodeValue() == 200) {
            log.info("Set Case Data for Case {}", caseUUID);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not Update Case; response: %s", response.getStatusCodeValue()), CASE_UPDATE_FAILURE);
        }
    }
}
