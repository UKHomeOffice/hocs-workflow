package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MigrationExecutionRequest {
    private MigrationPlan migrationPlan;
    private String[] processInstanceIds;
}
