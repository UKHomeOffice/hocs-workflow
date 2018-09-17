package uk.gov.digital.ho.hocs.workflow.documentClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.CreateCaseworkDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.CreateCaseworkDocumentResponse;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.ProcessDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.model.*;

import java.nio.charset.Charset;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class DocumentClient {

    private final String documentServiceUrl;
    private final String caseServiceAuth;

    private final String documentQueue;

    private final ProducerTemplate producerTemplate;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public DocumentClient(RestTemplate restTemp,
                          ProducerTemplate producerTemplate,
                          ObjectMapper objectMapper,
                          @Value("${hocs.document-service}") String documentService,
                          @Value("${hocs.case-service.auth}") String caseworkBasicAuth,
                          @Value("${docs.queue}") String documentQueue) {
        this.producerTemplate = producerTemplate;
        this.documentServiceUrl = documentService;
        this.objectMapper = objectMapper;
        this.caseServiceAuth = caseworkBasicAuth;
        this.restTemplate = restTemp;
        this.documentQueue = documentQueue;
    }

    public UUID createDocument(UUID caseUUID, String displayName, DocumentType type){
        log.debug("Creating Document, Case {}", caseUUID);
        CreateCaseworkDocumentRequest request = new CreateCaseworkDocumentRequest(displayName, type);
        ResponseEntity<CreateCaseworkDocumentResponse> response = postWithAuth(String.format("/case/%s/document", caseUUID), request, CreateCaseworkDocumentResponse.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Created Document {}, Case {}", response.getBody().getUuid(), caseUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Document; response: %s", response.getStatusCodeValue());
        }
    }


    public void processDocument(UUID caseUUID, UUID documentUUID, String fileLocation) {
        log.debug("Setting Document, Case {}", caseUUID);
        ProcessDocumentRequest request = new ProcessDocumentRequest(documentUUID, caseUUID, fileLocation);

        try {
            producerTemplate.sendBody(documentQueue, objectMapper.writeValueAsString(request));
            log.info("Set Document, Case {}", caseUUID);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        }
    }

    public void deleteDocument(UUID caseUUID, UUID documentUUID) {
        log.debug("Deleting Document {}, Case {}", documentUUID, caseUUID);

        ResponseEntity<Void> response = deleteWithAuth(String.format("/case/%s/document/%s", caseUUID, documentUUID), null,  Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Deleted Document {}, Case {}", documentUUID, caseUUID);
        } else {
            throw new EntityCreationException("Could not delete Document; response: %s", response.getStatusCodeValue());
        }
    }

    private <T,R> ResponseEntity<R> deleteWithAuth(String url, T request, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", documentServiceUrl, url), HttpMethod.DELETE, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private <T,R> ResponseEntity<R> postWithAuth(String url, T request, Class<R> responseType) {
        return restTemplate.postForEntity(String.format("%s%s", documentServiceUrl, url), new HttpEntity<>(request, createAuthHeaders()), responseType);
    }

    private <R> ResponseEntity<R> getWithAuth(String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", documentServiceUrl, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, caseworkBasicAuth());
        return headers;
    }

    private String caseworkBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(caseServiceAuth.getBytes(Charset.forName("UTF-8")))); }

}
