package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class MigrationExecutionRequest {
    final private MigrationPlan migrationPlan;
    final private String[] processInstanceIds;
}
