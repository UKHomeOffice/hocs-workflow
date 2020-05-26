package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Test
    public void getSchemasForCaseTypeAndStages() {
        String caseType = "case type";
        String caseStages = "STAGE1,STAGE2";
        String expectedResourcePath = String.format("/schema/caseType/%s?stages=%s", caseType, caseStages);

        SchemaDto schemaDto = new SchemaDto();

        when(restHelper.get(eq(infoServiceUrl), eq(expectedResourcePath), any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(schemaDto));
        List<SchemaDto> result = infoClient.getSchemasForCaseTypeAndStages(caseType, caseStages);

        Assert.assertEquals(List.of(schemaDto), result);
        verify(restHelper).get(eq(infoServiceUrl), eq(expectedResourcePath), any(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restHelper);
    }

}
