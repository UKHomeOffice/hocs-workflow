package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum StageStatusType {
    CREATED("CREATED"),
    UPDATED("UPDATED"),
    REJECTED("REJECTED"),
    COMPLETE("COMPLETE");

    @Getter
    private String displayValue;

    StageStatusType(String value) {
        displayValue = value;
    }
}
