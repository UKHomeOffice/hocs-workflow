package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum StageType {

    DCU_MIN_MARKUP(1),
    UKVI_BREF_CATEGORISE(2);

    @Getter
    private int numVal;

    private String displayName;

    StageType(int numVal) {
        this.numVal = numVal;
    }

}
