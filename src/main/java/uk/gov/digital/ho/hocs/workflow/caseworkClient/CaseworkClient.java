package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.DocumentType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Component
public class CaseworkClient {

    private final String CASE_SERVICE;
    private final RestTemplate restTemplate;

    @Autowired
    public CaseworkClient(RestTemplate restTemp, @Value("${hocs.case-service}") String caseService) {
        CASE_SERVICE = caseService;
        restTemplate = restTemp;
    }

    public CwCreateCaseResponse createCase(CaseType caseType) throws EntityCreationException {
        log.info("Creating a case: {}", caseType);
        if(caseType != null) {
            CreateCaseRequest request = new CreateCaseRequest(caseType);
            ResponseEntity<CwCreateCaseResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case", request, CwCreateCaseResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully created case: {}", caseType);
                return response.getBody();
            } else {
                throw new EntityCreationException("Could not create case; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create case; caseUUID or caseType is null!");
        }
    }

    public CwCreateStageResponse createStage(UUID caseUUID, StageType stageType) throws EntityCreationException {
        log.info("Creating a stage for case: '{}' -  type: '{}'", caseUUID, stageType);
        if(caseUUID != null && stageType != null) {
            CreateStageRequest request = new CreateStageRequest(stageType, new HashMap<>());
            ResponseEntity<CwCreateStageResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/stage" , request, CwCreateStageResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully created stage: '{}' - '{}' - '{}'", caseUUID, response.getBody().getUuid(), stageType);
                return response.getBody();
            } else {
                throw new EntityCreationException("Could not create stage; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not create stage; caseUUID or caseType is null!");
        }
    }

    public CwCreateDocumentResponse addDocument(UUID caseUUID, String name, DocumentType type) throws EntityCreationException {
        log.info("Creating document for case '{}'", caseUUID);
        if(caseUUID != null && name != null && type != null) {
            CreateDocumentRequest request = new CreateDocumentRequest();
            ResponseEntity<CwCreateDocumentResponse> response = restTemplate.postForEntity(CASE_SERVICE + "/case/" + caseUUID + "/document", request, CwCreateDocumentResponse.class);
            if(response.getStatusCodeValue() == 200) {
                log.debug("Successfully added document ('{}') to case: '{}'", response.getBody().getUuid(), caseUUID);
                return response.getBody();
            } else {
                throw new EntityCreationException("Could not create document; response: " + response.getStatusCodeValue());
            }
        } else {
            throw new EntityCreationException("Could not add document; caseUUID or caseType is null!");
        }
    }
}
