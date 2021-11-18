package uk.gov.digital.ho.hocs.workflow.client.documentclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.dto.CreateCaseworkDocumentRequest;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Slf4j
@Component
public class DocumentClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;

    @Autowired
    public DocumentClient(RestHelper restHelper,
                          @Value("${hocs.document-service}") String documentService){
        this.restHelper = restHelper;
        this.serviceBaseURL = documentService;
    }

    public void createDocument(
            UUID caseUUID,
            UUID actionDataItemUuid,
            String displayName,
            String fileLocation,
            String type
    ) {
        CreateCaseworkDocumentRequest request =
                new CreateCaseworkDocumentRequest(displayName, type, fileLocation, caseUUID, actionDataItemUuid);
        UUID response = restHelper.post(serviceBaseURL, "/document", request, UUID.class);
        log.info("Created Document {}, Case {}", response, caseUUID, value(EVENT, DOCUMENT_CLIENT_CREATE_SUCCESS));
    }

    public void createDocument(
            UUID caseUUID,
            String displayName,
            String fileLocation,
            String type
    ) {
        CreateCaseworkDocumentRequest request =
                new CreateCaseworkDocumentRequest(displayName, type, fileLocation, caseUUID);
        UUID response = restHelper.post(serviceBaseURL, "/document", request, UUID.class);
        log.info("Created Document {}, Case {}", response, caseUUID, value(EVENT, DOCUMENT_CLIENT_CREATE_SUCCESS));
    }

    public String getDocumentName(UUID documentUUID) {
        final String response = restHelper.get(serviceBaseURL, String.format("/document/%s/name", documentUUID), String.class);
        log.info("Get Document Name: {} for UUID {}", response, documentUUID);
        return response;
    }
}
