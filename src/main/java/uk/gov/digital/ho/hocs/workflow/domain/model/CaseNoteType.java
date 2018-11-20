package uk.gov.digital.ho.hocs.workflow.domain.model;

import lombok.Getter;

public enum CaseNoteType {

        MANUAL("MANUAL"),
        SYSTEM("SYSTEM");

        @Getter
        private String displayValue;

        CaseNoteType(String value) {
            displayValue = value;
        }
}
