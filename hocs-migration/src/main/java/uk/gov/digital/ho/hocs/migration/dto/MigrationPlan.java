package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MigrationPlan {

    final private String sourceProcessDefinitionId;

    final private String targetProcessDefinitionId;

    final private Instruction[] instructions;
}
