package uk.gov.digital.ho.hocs.workflow.client.documentclient.model;

import lombok.Getter;

public enum DocumentStatus {

    PENDING("Pending"),
    UPLOADED("Uploaded"),
    FAILED("Failed");

    @Getter
    private String displayValue;

    DocumentStatus(String value) {
        displayValue = value;
    }
}
