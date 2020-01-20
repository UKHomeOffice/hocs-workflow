package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class InfoClientTest {

    private InfoClient infoClient;

    @Mock
    private RestHelper restHelper;

    private String infoServiceUrl = "http://localhost:8082";


    @Before
    public void setup() {
        infoClient = new InfoClient(restHelper, infoServiceUrl);
    }

    @Test
    public void getCaseDetailsFieldsByCaseType() {
        String caseType = "type";
        String expectedUrl = String.format("/caseDetailsFields/%s", caseType);

        infoClient.getCaseDetailsFieldsByCaseType(caseType);

        verify(restHelper).get(eq(infoServiceUrl), eq(expectedUrl), any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(restHelper);
    }

}
