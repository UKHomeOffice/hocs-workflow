package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MigrationPlanError {
    private Instruction instruction;
    private String[] failures;
}
