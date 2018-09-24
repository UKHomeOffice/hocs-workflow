package uk.gov.digital.ho.hocs.workflow.documentClient.model;

import lombok.Getter;

public enum ManagedDocumentStatus {

    PENDING("Pending"),
    ACTIVE("Active"),
    EXPIRED("Expired");

    @Getter
    private String displayValue;

    ManagedDocumentStatus(String value) {
        displayValue = value;
    }
}
