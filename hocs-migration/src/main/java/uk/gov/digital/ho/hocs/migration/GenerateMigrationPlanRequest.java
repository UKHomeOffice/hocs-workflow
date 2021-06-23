package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GenerateMigrationPlanRequest {

    private String sourceProcessDefinitionId;

    private String targetProcessDefinitionId;

    private String updateEventTriggers;
}
