package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;

@Getter
public class MigrationPlanValidationResult {
        private MigrationPlanError[] instructionReports;
}

