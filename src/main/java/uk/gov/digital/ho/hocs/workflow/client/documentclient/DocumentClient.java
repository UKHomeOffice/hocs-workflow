package uk.gov.digital.ho.hocs.workflow.client.documentclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.dto.CreateCaseworkDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.dto.CreateCaseworkDocumentResponse;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.dto.ProcessDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Slf4j
@Component
public class DocumentClient {

    private final String documentQueue;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public DocumentClient(RestHelper restHelper,
                          @Value("${hocs.document-service}") String documentService,
                          ProducerTemplate producerTemplate,
                          @Value("${docs.queue}") String documentQueue,
                          ObjectMapper objectMapper){
        this.restHelper = restHelper;
        this.serviceBaseURL = documentService;
        this.producerTemplate = producerTemplate;
        this.documentQueue = documentQueue;
        this.objectMapper = objectMapper;
    }

    public UUID createDocument(UUID caseUUID, DocumentSummary document){
        CreateCaseworkDocumentRequest request = new CreateCaseworkDocumentRequest(document.getDisplayName(), document.getType(), caseUUID);
        CreateCaseworkDocumentResponse response = restHelper.post(serviceBaseURL, "/document", request, CreateCaseworkDocumentResponse.class);
        processDocument(response.getUuid(), document.getS3UntrustedUrl());
        log.info("Created Document {}, Case {}", response.getUuid(), caseUUID, value(EVENT, DOCUMENT_CLIENT_CREATE_SUCCESS));
        return response.getUuid();
    }

    private void processDocument(UUID documentUUID, String fileLocation) {
        ProcessDocumentRequest request = new ProcessDocumentRequest(documentUUID, fileLocation);
        try {
            sendMessage(objectMapper.writeValueAsString(request));
            log.info("Processed Document {}", documentUUID, value(EVENT, DOCUMENT_CLIENT_PROCESS_SUCCESS));
        } catch (JsonProcessingException e) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Could not process Document: %s", e.toString()), DOCUMENT_CLIENT_FAILURE);
        }
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    private void sendMessage(String message) {
        producerTemplate.sendBody(documentQueue, message);
    }
}
