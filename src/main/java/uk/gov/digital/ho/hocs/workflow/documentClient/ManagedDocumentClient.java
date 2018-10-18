package uk.gov.digital.ho.hocs.workflow.documentClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.CreateCaseworkManagedDocumentRequest;
import uk.gov.digital.ho.hocs.workflow.documentClient.dto.CreateCaseworkManagedDocumentResponse;
import uk.gov.digital.ho.hocs.workflow.documentClient.model.ManagedDocumentType;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;

import java.util.UUID;

@Slf4j
@Component
public class ManagedDocumentClient {

    private final String documentQueue;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public ManagedDocumentClient(RestHelper restHelper,
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

    public UUID createDocument(UUID referenceUUID, String displayName, ManagedDocumentType type){
        CreateCaseworkManagedDocumentRequest request = new CreateCaseworkManagedDocumentRequest(displayName, type, referenceUUID);
        ResponseEntity<CreateCaseworkManagedDocumentResponse> response = restHelper.post(serviceBaseURL, "/managedDocument", request, CreateCaseworkManagedDocumentResponse.class);
        if(response.getStatusCodeValue() == 200) {
            log.info("Created Managed Document {}, Reference {}", response.getBody().getUuid(), referenceUUID);
            return response.getBody().getUuid();
        } else {
            throw new EntityCreationException("Could not create Managed Document; response: %s", response.getStatusCodeValue());
        }
    }

    public void processDocument(UUID referenceUUID, UUID documentUUID, String fileLocation) {
        //ProcessDocumentRequest request = new ProcessDocumentRequest(documentUUID, referenceUUID, fileLocation);

        /* We need to add a fork into docs service before we use this */
        throw new NotImplementedException();
        //try {
        //    producerTemplate.sendBody(documentQueue, objectMapper.writeValueAsString(request));
        //    log.info("Set Document, Reference {}", referenceUUID);
        //} catch (JsonProcessingException e) {
        //    throw new EntityCreationException("Could not set Input Data: %s", e.toString());
        //}
    }

    public void deleteDocument(UUID documentUUID) {
        ResponseEntity<Void> response = restHelper.delete(serviceBaseURL, String.format("/managedDocument/%s", documentUUID), Void.class);

        if(response.getStatusCodeValue() == 200) {
            log.info("Deleted Managed Document {}, Case {}", documentUUID);
        } else {
            throw new EntityCreationException("Could not delete Managed Document; response: %s", response.getStatusCodeValue());
        }
    }
}
