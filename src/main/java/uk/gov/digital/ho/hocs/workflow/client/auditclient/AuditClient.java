package uk.gov.digital.ho.hocs.workflow.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

  /*  public void createCaseComplaintType() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ObjectMapper objectMapper = new ObjectMapper();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString("Test message created");
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, randomUUID("TEST1234567"), data, EventType.COMPLAINT_TYPE_CREATED, randomUUID("TET098134"),
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

            auditSnsClient.publish(publishRequest);
            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}", eventType, caseUUID, correlationId, userId, value(EVENT, AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}", caseUUID, value(EVENT, AUDIT_FAILED), value(EXCEPTION, e));
        }
    }

   private Map<String, MessageAttributeValue> getQueueHeaders(String eventType) {
        return Map.of(
                EVENT_TYPE_HEADER, new SnsStringMessageAttributeValue(eventType),
                RequestData.CORRELATION_ID_HEADER, new SnsStringMessageAttributeValue(requestData.correlationId()),
                RequestData.USER_ID_HEADER, new SnsStringMessageAttributeValue(requestData.userId()),
                RequestData.USERNAME_HEADER, new SnsStringMessageAttributeValue(requestData.username()),
                RequestData.GROUP_HEADER, new SnsStringMessageAttributeValue(requestData.groups()));
    }

    private static UUID randomUUID(String shortCode) {
        if (shortCode != null) {
            String uuid = UUID.randomUUID().toString().substring(0, 33);
            return UUID.fromString(uuid.concat(shortCode));
        } else {
            throw new ApplicationExceptions.EntityCreationException("shortCode is null", CASE_CREATE_FAILURE);
        }
    }*/
}
