package uk.gov.digital.ho.hocs.workflow.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetFormResponse;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormAccordion;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsFormSecondaryAction;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.ScreenRepository;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service("HocsFormService")
public class FormService {

    private final CaseworkClient caseworkClient;

    private final ScreenRepository screenRepository;

    private final List<String> additionalScreenFolders;

    public FormService(CaseworkClient caseworkClient,
                       ScreenRepository screenRepository,
                       @Value("${hocs.screens.additionalFolders}") List<String> fieldFolders) {
        this.caseworkClient = caseworkClient;
        this.screenRepository = screenRepository;

        this.additionalScreenFolders = fieldFolders;
        //TODO: HOCS-5932: magic word - can/do we extract this?
        this.additionalScreenFolders.add("live");
    }

    public GetFormResponse getFormWithCase(UUID caseUUID, String formName) {
        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(caseUUID);
        HocsSchema schema = getFormSchema(formName);
        HocsForm form = new HocsForm(schema, caseData.getData());
        return new GetFormResponse(caseData.getReference(), form);
    }

    public HocsSchema getFormSchema(String formName) {
        Schema schemaDto = additionalScreenFolders.stream().map(
                subFolder -> screenRepository.getSchema(formName, subFolder))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new ApplicationExceptions.ScreenNotFoundException(
                String.format("Screen %s could not be found", formName),
                LogEvent.SCREEN_NOT_FOUND));

        List<HocsFormField> fields = schemaDto.getFields().stream().map(HocsFormField::from).toList();
        fields = HocsFormAccordion.loadFormAccordions(fields);

        List<HocsFormSecondaryAction> secondaryActions = schemaDto.getSecondaryActions().stream().map(
            HocsFormSecondaryAction::from).toList();
        return new HocsSchema(schemaDto.getTitle(), schemaDto.getDefaultActionLabel(), fields,
            secondaryActions, schemaDto.getProps(), schemaDto.getValidation(), schemaDto.getSummary());
    }

}
