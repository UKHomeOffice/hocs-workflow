package uk.gov.digital.ho.hocs.workflow.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.domain.CaseData;
import uk.gov.digital.ho.hocs.workflow.util.SnsStringMessageAttributeValue;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.CreateAuditRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.AUDIT_EVENT_CREATED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.AUDIT_FAILED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EXCEPTION;

@Slf4j
//@Component
public class AuditClient {
    private static final String EVENT_TYPE_HEADER = "event_type";
    private final String auditQueue;
    private final String raisingService;
    private final String namespace;
    private final AmazonSNSAsync auditSearchSnsClient;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;
    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public AuditClient(AmazonSNSAsync auditSearchSnsClient,
                       @Value("${aws.sns.audit-search.arn}") String auditQueue,
                       @Value("${auditing.deployment.name}") String raisingService,
                       @Value("${auditing.deployment.namespace}") String namespace,
                       ObjectMapper objectMapper,
                       RequestData requestData,
                       RestHelper restHelper,
                       @Value("${hocs.audit-service}") String auditService) {
        this.auditSearchSnsClient = auditSearchSnsClient;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
        this.restHelper = restHelper;
        this.serviceBaseURL = auditService;
    }

    public void createCaseComplaintType() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(null);
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, null, data, EventType.COMPLAINT_TYPE_CREATED, null,
                requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    private void logFailedToParseDataPayload(JsonProcessingException e) {
        log.error("Failed to parse data payload, event {}, exception: {}", value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

    private void sendAuditMessage(LocalDateTime localDateTime, UUID caseUUID, String payload, EventType eventType, UUID stageUUID, String correlationId, String userId, String username, String groups) {
        sendAuditMessage(localDateTime, null, payload, eventType, stageUUID, "{}", correlationId, userId, username, groups);
    }
    private void sendAuditMessage(LocalDateTime localDateTime, UUID caseUUID, String payload, EventType eventType,
                                  UUID stageUUID, String data, String correlationId, String userId,
                                  String username, String groups) {
        CreateAuditRequest request = new CreateAuditRequest(
                correlationId,
                caseUUID,
                stageUUID,
                raisingService,
                payload,
                data,
                namespace,
                localDateTime,
                eventType,
                userId);

        try {
            var publishRequest = new PublishRequest(auditQueue, objectMapper.writeValueAsString(request))
                    .withMessageAttributes(getQueueHeaders(eventType.toString()));

            auditSearchSnsClient.publish(publishRequest);
            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}", eventType, caseUUID, correlationId, userId, value(EVENT, AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}", caseUUID, value(EVENT, AUDIT_FAILED), value(EXCEPTION, e));
        }
    }

    public void createCaseAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString("Test data created");
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseData.getUuid(), data, EventType.CASE_CREATED, null, data, requestData.correlationId(),
                requestData.userId(), requestData.username(), requestData.groups());
    }

    private void logFailedToParseAuditPayload(JsonProcessingException e){
        log.error("Failed to parse audit payload, event {}, exception: {}", value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

    private Map<String, MessageAttributeValue> getQueueHeaders(String eventType) {
        return Map.of(
                EVENT_TYPE_HEADER, new SnsStringMessageAttributeValue(eventType),
                RequestData.CORRELATION_ID_HEADER, new SnsStringMessageAttributeValue(requestData.correlationId()),
                RequestData.USER_ID_HEADER, new SnsStringMessageAttributeValue(requestData.userId()),
                RequestData.USERNAME_HEADER, new SnsStringMessageAttributeValue(requestData.username()),
                RequestData.GROUP_HEADER, new SnsStringMessageAttributeValue(requestData.groups()));
    }
}
