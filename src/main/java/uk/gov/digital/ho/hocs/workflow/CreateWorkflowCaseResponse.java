package uk.gov.digital.ho.hocs.workflow;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class CreateWorkflowCaseResponse {

    private final String caseReference;

    private final UUID uuid;

    private final StageType stageType;

    private final String spec;
}
