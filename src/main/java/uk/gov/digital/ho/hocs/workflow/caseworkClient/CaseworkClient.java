package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CaseworkClient {

    private final String CASE_SERVICE;
    private final RestTemplate restTemplate;

    @Autowired
    public CaseworkClient(RestTemplate restTemp, @Value("${hocs.case-service}") String caseService) {
        CASE_SERVICE = caseService;
        restTemplate = restTemp;
    }

    public String createCase(UUID caseUUID, CaseType caseType) throws EntityCreationException {
        log.info("Creating a case: '{}' - '{}'", caseUUID, caseType);
        if(caseUUID != null && caseType != null) {
            CreateCaseRequest request = new CreateCaseRequest(caseUUID, caseType);
            ResponseEntity<CreateCaseResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case", request, CreateCaseResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully created case: '{}' - '{}'", caseUUID, caseType);
                return response.getBody().getCaseReference();
            } else {
                throw new EntityCreationException("Could not create case, response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create case, caseUUID or caseType is null!");
        }
    }

    public void createStage(UUID caseUUID, UUID stageUUID, StageType stageType) throws EntityCreationException {
        log.info("Creating a stage: '{}' - '{}' - '{}'", caseUUID, stageUUID, stageType);
        if(caseUUID != null && stageUUID != null && stageType != null) {
            CreateStageRequest request = new CreateStageRequest(stageUUID, stageType, new HashMap<>());
            ResponseEntity<Void> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/stage" , request, Void.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully created stage: '{}' - '{}' - '{}'", caseUUID, stageUUID, stageType);
            } else {
                throw new EntityCreationException("Could not create stage, response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create stage, caseUUID or caseType is null!");
        }
    }

    public void addDocuments(UUID caseUUID, List<CaseworkDocumentSummary> documentSummaries) throws EntityCreationException {
        log.info("Adding documents to case '{}'", caseUUID);
        if(caseUUID != null && documentSummaries != null && !documentSummaries.isEmpty()) {
            AddDocumentsRequest request = new AddDocumentsRequest(documentSummaries);
            ResponseEntity<Void> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/documents", request, Void.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully added documents ('{}') to case: '{}'", documentSummaries.size(), caseUUID);
            } else {
                throw new EntityCreationException("Could not create stage, response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not add documents, caseUUID or caseType is null!");
        }
    }
}
