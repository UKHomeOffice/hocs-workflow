package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum CaseType {
    MIN("MIN"),
    TRO("TRO"),
    DTEN("DTEN");

    @Getter
    private String displayValue;

    CaseType(String value) {
        displayValue = value;
    }
}