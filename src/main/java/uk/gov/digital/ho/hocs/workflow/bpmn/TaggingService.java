package uk.gov.digital.ho.hocs.workflow.bpmn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseTagRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;

import java.util.UUID;

@Service
@Slf4j
public class TaggingService {
    private final CaseworkClient caseWorkClient;

    public TaggingService(CaseworkClient caseWorkClient) {
        this.caseWorkClient = caseWorkClient;
    }

    public void createTagForCase(String caseUUIDString, String tag) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        CaseTagRequest caseTagRequest = new CaseTagRequest(tag);
        log.info("Set case data for case {} and tag - {}", caseUUIDString, caseTagRequest.getTag());
        caseWorkClient.createCaseTag(caseUUID, caseTagRequest);
    }
}
