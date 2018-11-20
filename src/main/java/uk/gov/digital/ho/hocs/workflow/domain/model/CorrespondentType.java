package uk.gov.digital.ho.hocs.workflow.domain.model;

import lombok.Getter;

public enum CorrespondentType {
    CORRESPONDENT("CORRESPONDENT"),
    CONSTITUENT("CONSTITUENT"),
    THIRD_PARTY("THIRD_PARTY"),
    APPLICANT("APPLICANT"),
    COMPLAINANT("COMPLAINANT"),
    FAMILY("FAMILY_RELATION"),
    FRIEND("FRIEND"),
    LEGAL_REP("LEGAL_REP"),
    MEMBER("MEMBER"),
    OTHER("OTHER");

    @Getter
    private String displayValue;

    CorrespondentType(String value) {
        displayValue = value;
    }
}
