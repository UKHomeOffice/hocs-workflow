package uk.gov.digital.ho.hocs.workflow.client.documentclient.model;

import lombok.Getter;

public enum DocumentType {

    ORIGINAL("Original"),
    DRAFT("Draft"),
    FINAL("Final");

    @Getter
    private String displayValue;

    DocumentType(String value) {
        displayValue = value;
    }
}
