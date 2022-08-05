package uk.gov.digital.ho.hocs.workflow.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.SecondaryActionDto;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormServiceTest {

    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private InfoClient infoClient;

    private FormService formService;

    @Before
    public void beforeTest() {
        formService = new FormService(
                caseworkClient,
                infoClient);
    }


    @Test
    public void getFormWithCase() {
        var caseUuid = UUID.randomUUID();
        var screenName = "TEST_SCREEN";

        var getCaseworkCaseDataResponse = new GetCaseworkCaseDataResponse(caseUuid, null, null, "TEST/0000000/00", Map.of("Test", "Yes"),
                null, null, null, null, null, null, null);
        when(caseworkClient.getCase(caseUuid)).thenReturn(getCaseworkCaseDataResponse);
        when(infoClient.getSchema(screenName)).thenReturn(exampleSchemaDto());

        var response = formService.getFormWithCase(caseUuid, screenName);

        assertThat(response.getCaseReference()).isSameAs(getCaseworkCaseDataResponse.getReference());
        assertThat(response.getForm().getSchema().getTitle()).isSameAs("Test Schema Title");
        assertThat(response.getForm().getSchema().getDefaultActionLabel()).isEqualTo("Continue");
        assertThat(response.getForm().getSchema().getFields().get(0).getComponent()).isEqualTo("text");
        assertThat(response.getForm().getSchema().getFields().get(0).getValidation()).isEmpty();
        assertThat(response.getForm().getSchema().getFields().get(0).getProps())
                .containsEntry("label", "TestLabel")
                .containsEntry("name", "TestField");
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getComponent()).isSameAs("button");
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getValidation()).isEmpty();
        assertThat(response.getForm().getSchema().getSecondaryActions().get(0).getProps())
                .containsEntry("label", "TestSecondaryLabel")
                .containsEntry("name", "TestSecondaryAction");
        assertThat(response.getForm().getSchema().getProps()).isNotNull();
        assertThat(response.getForm().getSchema().getValidation()).isNotNull();
        assertThat(response.getForm().getData()).isSameAs(getCaseworkCaseDataResponse.getData());
    }

    private SchemaDto exampleSchemaDto() {
        FieldDto fieldDto = new FieldDto(UUID.randomUUID(), "TestField", "TestLabel", "text", new Object[0],
                new HashMap<>(), false, false, null);
        List<FieldDto> fields = List.of(fieldDto);

        SecondaryActionDto secondaryActionDto = new SecondaryActionDto(UUID.randomUUID(), "TestSecondaryAction",
                "TestSecondaryLabel", "button", new String[0], new HashMap<>());
        List<SecondaryActionDto> secondaryActions = List.of(secondaryActionDto);

        return new SchemaDto(UUID.randomUUID(), "STAGE", "SCHEMA_TYPE", "Test Schema Title", "Continue",
                true, fields, secondaryActions, new Object(), new Object(), new ArrayList<>());
    }


}
