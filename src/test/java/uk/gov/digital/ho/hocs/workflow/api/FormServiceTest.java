package uk.gov.digital.ho.hocs.workflow.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class FormServiceTest {

    private static UUID caseUuid;

    private static GetCaseworkCaseDataResponse caseworkResponse;

    @MockBean
    private CaseworkClient caseworkClient;

    @Autowired
    private FormService formService;

    @Before
    public void setup() {
        caseUuid = UUID.randomUUID();
        caseworkResponse = new GetCaseworkCaseDataResponse(caseUuid, null, null, "TEST/0000000/00",
            Map.of("Test", "Yes"), null, null, null, null, null, null, null);
        when(caseworkClient.getCase(caseUuid)).thenReturn(caseworkResponse);
    }

    @Test
    public void getFormWithCaseWhenScreenOnlyInLiveFolder() {
        var response = formService.getFormWithCase(caseUuid, "TEST_SCREEN");

        assertThat(response.getCaseReference()).isSameAs(caseworkResponse.getReference());
        assertThat(response.getForm().getSchema()).isNotNull();
        assertThat(response.getForm().getData()).isSameAs(caseworkResponse.getData());
    }

    @Test
    public void getFormSchemaWhenScreenOnlyInLiveFolder() {
        var response = formService.getFormSchema("TEST_SCREEN");

        assertThat(response.getTitle()).isEqualTo("Test Screen Title");
        assertThat(response.getDefaultActionLabel()).isEqualTo("Continue");
        assertThat(response.getFields().get(0).getComponent()).isEqualTo("text");
        assertThat(response.getFields().get(0).getValidation()).isEqualTo(new String[] {"required"});
        assertThat(response.getFields().get(0).getProps()).containsEntry("label",
            "Test Field 1").containsEntry("name", "TestField");
        assertThat(response.getSecondaryActions().get(0).getComponent()).isEqualTo("backButton");
        assertThat(response.getSecondaryActions().get(0).getValidation()).isEmpty();
        assertThat(response.getSecondaryActions().get(0).getProps()).containsEntry("label",
            "Back").containsEntry("name", "backButton");
        assertThat(response.getProps()).isNotNull();
        assertThat(response.getValidation()).isNotNull();
    }

    @Test
    public void getFormWithCaseWhenScreenOnlyInBetaFolder() {
        var response = formService.getFormSchema("BETA_TEST_SCREEN");

        assertThat(response.getTitle()).isEqualTo("Beta Screen Title");
    }

    @Test(expected = ApplicationExceptions.ScreenNotFoundException.class)
    public void getFormSchemaThrowsExceptionIfNotPresent() {
        formService.getFormSchema("UNKNOWN");
    }

}
