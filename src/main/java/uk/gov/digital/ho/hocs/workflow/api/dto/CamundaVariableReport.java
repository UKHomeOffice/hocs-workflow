package uk.gov.digital.ho.hocs.workflow.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.CaseExecution;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.CaseTask;

@AllArgsConstructor()
@Getter
public class CamundaVariableReport {
  private final List<CaseExecution> caseExecutions;
  private final List<CaseExecution> stageExecutions;
  private final CaseTask task;
}
