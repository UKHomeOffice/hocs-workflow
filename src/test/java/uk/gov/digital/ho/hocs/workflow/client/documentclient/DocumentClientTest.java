package uk.gov.digital.ho.hocs.workflow.client.documentclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.documentclient.dto.CreateCaseworkDocumentRequest;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentClientTest {

    private DocumentClient documentClient;

    @Mock
    private RestHelper restHelper;

    private String documentServiceUrl = "http://localhost:8083";


    @Before
    public void setup() {
        documentClient = new DocumentClient(restHelper, documentServiceUrl);
    }

    @Test
    public void createDocument() {
        // given
        UUID caseUuid = UUID.randomUUID();
        String expectedUrl = String.format("/document");

        CreateCaseworkDocumentRequest request = new CreateCaseworkDocumentRequest(
                "displayName",
                "type",
                "fileLocation",
                caseUuid,
                null
        );

        // when
        documentClient.createDocument(caseUuid,request);

        // then
        ArgumentCaptor<CreateCaseworkDocumentRequest> argumentCaptor =
                ArgumentCaptor.forClass(CreateCaseworkDocumentRequest.class);

        verify(restHelper).post(eq(documentServiceUrl), eq(expectedUrl), argumentCaptor.capture(), eq(UUID.class));

        final CreateCaseworkDocumentRequest result = argumentCaptor.getValue();

        assertThat(result.getExternalReferenceUUID()).isEqualTo(caseUuid);

        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void createDocumentForAction() {
        // given
        UUID caseUuid = UUID.randomUUID();
        UUID actionDataItemUuid = UUID.randomUUID();
        String expectedUrl = String.format("/document");

        CreateCaseworkDocumentRequest request = new CreateCaseworkDocumentRequest(
                "displayName",
                "type",
                "fileLocation",
                caseUuid,
                actionDataItemUuid
        );

        // when
        documentClient.createDocument(caseUuid, request);

        // then
        ArgumentCaptor<CreateCaseworkDocumentRequest> argumentCaptor =
                ArgumentCaptor.forClass(CreateCaseworkDocumentRequest.class);

        verify(restHelper).post(eq(documentServiceUrl), eq(expectedUrl), argumentCaptor.capture(), eq(UUID.class));

        final CreateCaseworkDocumentRequest result = argumentCaptor.getValue();

        assertThat(result.getActionDataItemUuid()).isEqualTo(actionDataItemUuid);
        assertThat(result.getExternalReferenceUUID()).isEqualTo(caseUuid);

        verifyNoMoreInteractions(restHelper);
    }

}
