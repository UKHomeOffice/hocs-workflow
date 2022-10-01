package uk.gov.digital.ho.hocs.workflow.api;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetFormResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormAccordion;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormSecondaryAction;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.ScreenRepository;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Schema;

import java.util.List;
import java.util.UUID;

@Service("HocsFormService")
public class FormService {

    private final CaseworkClient caseworkClient;

    private final ScreenRepository screenRepository;

    public FormService(CaseworkClient caseworkClient, ScreenRepository screenRepository) {
        this.caseworkClient = caseworkClient;
        this.screenRepository = screenRepository;
    }

    public GetFormResponse getFormWithCase(UUID caseUUID, String formName) {
        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(caseUUID);
        Schema schemaDto = screenRepository.getSchema(formName);

        List<HocsFormField> fields = schemaDto.getFields().stream()
            .map(HocsFormField::from).toList();
        fields = HocsFormAccordion.loadFormAccordions(fields);

        List<HocsFormSecondaryAction> secondaryActions = schemaDto.getSecondaryActions().stream().map(
            HocsFormSecondaryAction::from).toList();
        HocsSchema schema = new HocsSchema(schemaDto.getTitle(), schemaDto.getDefaultActionLabel(), fields,
            secondaryActions, schemaDto.getProps(), schemaDto.getValidation(), schemaDto.getSummary());
        HocsForm form = new HocsForm(schema, caseData.getData());
        return new GetFormResponse(caseData.getReference(), form);
    }

}
