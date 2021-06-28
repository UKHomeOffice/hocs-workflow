package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MigrationPlanError {
    final private Instruction instruction;
    final private String[] failures;
}
