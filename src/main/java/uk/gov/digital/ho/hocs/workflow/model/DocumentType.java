package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum DocumentType {
    ORIGINAL(0),
    SUBSEQUENT(1),
    DRAFT(2),
    FINAL_RESPONSE(3);

    @Getter
    private int numVal;

    DocumentType(int numVal) {
        this.numVal = numVal;
    }
}
