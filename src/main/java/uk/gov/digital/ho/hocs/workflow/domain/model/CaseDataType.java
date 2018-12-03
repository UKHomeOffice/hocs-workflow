package uk.gov.digital.ho.hocs.workflow.domain.model;

import lombok.Getter;

public enum CaseDataType {
    MIN("MIN", "a1"),
    TRO("TRO", "a2"),
    DTEN("DTEN", "a3");

    @Getter
    private String displayName;

    @Getter
    private String value;

    CaseDataType(String name, String val) {
        displayName = name;
        value = val;
    }
}