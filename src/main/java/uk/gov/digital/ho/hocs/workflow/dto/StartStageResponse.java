package uk.gov.digital.ho.hocs.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class StartStageResponse {

    //private final String caseReference;

    //private final UUID uuid;

    private final StageType stageType;

    private final String spec;
}
