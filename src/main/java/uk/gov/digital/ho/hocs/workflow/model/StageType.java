package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum StageType {

    DCU_MIN_MARKUP(100),
    DCU_MIN_TRANSFER_CONFIRMATION(101),
    UKVI_BREF_CATEGORISE(300);

    @Getter
    private int numVal;

    private String displayName;

    StageType(int numVal) {
        this.numVal = numVal;
    }

}
