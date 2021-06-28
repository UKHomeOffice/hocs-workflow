package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MigrationPlanValidationResult {
        final private MigrationPlanError[] instructionReports;
}

