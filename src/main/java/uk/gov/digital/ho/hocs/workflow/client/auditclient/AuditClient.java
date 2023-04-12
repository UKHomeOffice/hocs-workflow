package uk.gov.digital.ho.hocs.workflow.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.BusinessEventPayloadInterface;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.digital.ho.hocs.workflow.application.aws.SnsStringMessageAttributeValue;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.CreateAuditRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.AUDIT_FAILED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EXCEPTION;

@Slf4j
@Component
public class AuditClient {
    private static final String EVENT_TYPE_HEADER = "event_type";
    private final String auditQueue;
    private final String raisingService;
    private final String namespace;
    private final AmazonSNSAsync auditSnsClient;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;
    private final static String EMPTY_JSON = "{}";

    @Autowired
    public AuditClient(AmazonSNSAsync auditSnsClient,
                       @Value("${aws.sns.audit-search.arn}") String auditQueue,
                       @Value("${auditing.deployment.name}") String raisingService,
                       @Value("${auditing.deployment.namespace}") String namespace,
                       ObjectMapper objectMapper,
                       RequestData requestData) {
        this.auditSnsClient = auditSnsClient;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
    }

    public void createBusinessEvent(UUID caseUuid, UUID stageUuid, BusinessEventPayloadInterface businessEvent) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = EMPTY_JSON;
        try {
            data = objectMapper.writeValueAsString(businessEvent);
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }

        sendAuditMessage(localDateTime, caseUuid, data, businessEvent.getEventType(), stageUuid,
            requestData.correlationId(), requestData.userId());
    }

    private void logFailedToParseDataPayload(JsonProcessingException e) {
        log.error("Failed to parse data payload, event {}, exception: {}",
                value(EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(EXCEPTION, e));
    }

    private void sendAuditMessage(LocalDateTime localDateTime, UUID caseUUID, String payload, EventType eventType,
                                  UUID stageUUID, String correlationId, String userId) {
        sendAuditMessage(localDateTime, caseUUID, payload, eventType, stageUUID, "{}",
                correlationId, userId);
    }

    private void sendAuditMessage(LocalDateTime localDateTime, UUID caseUUID, String payload, EventType eventType,
                                  UUID stageUUID, String data, String correlationId, String userId) {
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

            auditSnsClient.publish(publishRequest);
            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}",
                eventType, caseUUID, correlationId, userId, value(EVENT, eventType));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}",
                caseUUID, value(EVENT, AUDIT_FAILED), value(EXCEPTION, e));
        }
    }

    public void reopenCase(UUID caseUuid) {
        LocalDateTime localDateTime = LocalDateTime.now();

        sendAuditMessage(
            localDateTime,
            caseUuid,
            null,
            EventType.CASE_REOPENED,
            null,
            requestData.correlationId(),
            requestData.userId()
        );
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
