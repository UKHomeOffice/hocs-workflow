package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MigrationPlan {

    private String sourceProcessDefinitionId;

    private String targetProcessDefinitionId;

    private Instruction[] instructions;
}
