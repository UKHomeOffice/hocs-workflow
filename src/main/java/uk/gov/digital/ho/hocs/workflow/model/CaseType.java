package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum CaseType {
    RSH(0),
    MIN(1),
    TRO(2),
    DTEN(3),
    BREF(4);

    @Getter
    private int numVal;

    CaseType(int numVal) {
        this.numVal = numVal;
    }
}
