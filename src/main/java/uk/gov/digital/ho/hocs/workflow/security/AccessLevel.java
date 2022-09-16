package uk.gov.digital.ho.hocs.workflow.security;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AccessLevel {

    UNSET(0),
    MIGRATE(1),
    RESTRICTED_OWNER(2),
    SUMMARY(3),
    READ(4),
    WRITE(5),
    OWNER(6),
    CASE_ADMIN(7);

    private int level;

    AccessLevel(int level) {
        this.level = level;
    }

    public static AccessLevel from(int value) {
        return Arrays.stream(values()).filter(level -> level.level == value).findFirst().orElseThrow(
            IllegalArgumentException::new);
    }
}
