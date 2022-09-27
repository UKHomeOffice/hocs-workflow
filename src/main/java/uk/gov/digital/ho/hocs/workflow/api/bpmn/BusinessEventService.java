package uk.gov.digital.ho.hocs.workflow.api.bpmn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.EventType;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.BusinessEvent;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.COMPLAINT_TYPE_FORMAT;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.COMPLAINT_TYPE_UPDATED_FORMAT;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.PREVIOUS_COMPLAINT_TYPE;
import static uk.gov.digital.ho.hocs.workflow.client.auditclient.EventType.COMPLAINT_TYPE_UPDATED;

@Service
@Slf4j
public class BusinessEventService {

    private final AuditClient auditClient;

    private final CaseworkClient caseworkClient;

    private final BpmnService bpmnService;

    public BusinessEventService(AuditClient auditClient, CaseworkClient caseworkClient, BpmnService bpmnService) {
        this.auditClient = auditClient;
        this.caseworkClient = caseworkClient;
        this.bpmnService = bpmnService;
    }

    public void createComplaintTypeEvent(String caseUUIDString, String stageUUIDString, String currentComplaintType){
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID= UUID.fromString(stageUUIDString);
        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(caseUUID);
        String previousComplaintType = caseData.getData().get(PREVIOUS_COMPLAINT_TYPE);

        String message = previousComplaintType == null || currentComplaintType.equalsIgnoreCase(previousComplaintType)
            ? String.format(COMPLAINT_TYPE_FORMAT, currentComplaintType)
            : String.format(COMPLAINT_TYPE_UPDATED_FORMAT, previousComplaintType, currentComplaintType);
        BusinessEvent businessEvent = new BusinessEvent(COMPLAINT_TYPE_UPDATED, message);

        auditClient.createBusinessEvent(caseUUID, stageUUID, businessEvent);
        bpmnService.updateCaseValue(caseUUIDString, stageUUIDString, PREVIOUS_COMPLAINT_TYPE, currentComplaintType);
    }



}
