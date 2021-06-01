package uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor()
@Getter
public class CaseExecution {

  private final String processDefinitionId;
  private final String businessKey;
  private final String processInstanceId;
  private final String executionId;
  private final Map<String, Object> variables;
}
