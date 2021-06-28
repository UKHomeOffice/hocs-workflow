package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MigrationPlanValidationResult {
        private MigrationPlanError[] instructionReports;
}

