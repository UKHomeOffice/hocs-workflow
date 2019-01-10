package uk.gov.digital.ho.hocs.workflow.domain.model;

import lombok.Getter;

public enum CaseDataType {
    MIN("MIN", "a1"),
    TRO("TRO", "a2"),
    DTEN("DTEN", "a3");

    @Getter
    private String type;

    @Getter
    private String shortCode;

    CaseDataType(String type, String shortCode) {
        this.type = type;
        this.shortCode = shortCode;
    }
}