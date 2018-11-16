package uk.gov.digital.ho.hocs.workflow.client.documentClient.model;

import lombok.Getter;

public enum DocumentType {

    ORIGINAL("Original"),
    DRAFT("Draft");

    @Getter
    private String displayValue;

    DocumentType(String value) {
        displayValue = value;
    }
}
