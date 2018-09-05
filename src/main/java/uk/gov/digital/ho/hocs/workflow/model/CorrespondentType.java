package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum CorrespondentType {
    CORRESPONDENT("Correspondent"),
    CONSTITUENT("Constituent"),
    THIRD_PARY("Third Party"),
    APPLICANT("Applicant"),
    COMPLAINANT("Complainant"),
    FAMILY("Family Relation"),
    FRIEND("Friend"),
    LEGAL_REP("Legal Representative"),
    MEMBER("Member"),
    OTHER("Other");

    @Getter
    private String displayValue;

    CorrespondentType(String value) {
        displayValue = value;
    }
}

