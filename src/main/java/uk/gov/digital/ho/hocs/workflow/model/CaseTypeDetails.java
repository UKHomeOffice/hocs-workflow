package uk.gov.digital.ho.hocs.workflow.model;

import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

@AllArgsConstructor
public class CaseTypeDetails {

    private String requiredRole;

    private String displayName;

    private CaseType value;
}
