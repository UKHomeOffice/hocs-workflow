package uk.gov.digital.ho.hocs.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MigrationExecutionRequest {
    final private MigrationPlan migrationPlan;
    final private String[] processInstanceIds;
}
