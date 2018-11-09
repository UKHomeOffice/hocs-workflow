package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum CaseDataType {
    RSH("WR Response"),
    MIN("Ministerial"),
    TRO("Treat Official"),
    DTEN("Number 10");

    @Getter
    private String displayValue;

    CaseDataType(String value) {
        displayValue = value;
    }
}

