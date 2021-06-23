package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;

@Getter
public class MigrationPlanError {
    private Instruction instruction;
    private String[] failures;
}
