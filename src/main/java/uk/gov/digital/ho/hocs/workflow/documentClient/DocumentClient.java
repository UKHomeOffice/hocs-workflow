package uk.gov.digital.ho.hocs.workflow.documentClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.CreateCaseworkDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.CreateCaseworkDocumentResponse;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.ProcessDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.documentClient.model.DocumentType;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;

import java.util.*;

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

    public UUID createDocument(UUID caseUUID, String displayName, DocumentType type){
        CreateCaseworkDocumentRequest request = new CreateCaseworkDocumentRequest(displayName, type, caseUUID);
        ResponseEntity<CreateCaseworkDocumentResponse> response = restHelper.post(serviceBaseURL, "/document", request, CreateCaseworkDocumentResponse.class);
        if(response.getStatusCodeValue() == 200) {
            log.info("Created Document {}, Case {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Document; response: %s", response.getStatusCodeValue());
        }
    }

    public void processDocument(UUID documentUUID, String fileLocation) {
        ProcessDocumentRequest request = new ProcessDocumentRequest(documentUUID, fileLocation);

        try {
            producerTemplate.sendBody(documentQueue, objectMapper.writeValueAsString(request));
            log.info("Processed Document {}", documentUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not process Document: %s", e.toString());
        }
    }

    public void deleteDocument(UUID caseUUID, UUID documentUUID) {
        ResponseEntity<Void> response = restHelper.delete(serviceBaseURL, String.format("/document/%s", documentUUID), Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Deleted Document {}, Case {}", documentUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not delete Document; response: %s", response.getStatusCodeValue());
        }
    }
}
