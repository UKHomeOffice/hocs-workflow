package uk.gov.digital.ho.hocs.workflow.security;

import lombok.Getter;

@Getter
public enum AccessLevel {
    UNSET(0), SUMMARY(1), READ(2), WRITE(3), OWNER(5);

    private int level;

    AccessLevel(int level) {
        this.level = level;
    }
}
