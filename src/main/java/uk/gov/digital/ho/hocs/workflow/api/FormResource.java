package uk.gov.digital.ho.hocs.workflow.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetFormResponse;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
class FormResource {

    private @Qualifier("HocsFormService") FormService formService;

    public FormResource(FormService formService) {
        this.formService = formService;
    }

    @GetMapping(value = "/case/{caseUUID}/form/{formName}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetFormResponse> getFormWithCase(@PathVariable UUID caseUUID, @PathVariable String formName) {
        GetFormResponse response = formService.getFormWithCase(caseUUID, formName);
        return ResponseEntity.ok(response);
    }

}
