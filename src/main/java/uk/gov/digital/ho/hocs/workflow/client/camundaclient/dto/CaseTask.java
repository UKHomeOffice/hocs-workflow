package uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor()
@Getter
public class CaseTask {

  private final String id;
  private final String definitionKey;
  private final String name;
  private final String formKey;
  private final String processDefinitionId;
  private final Map<String, Object> variables;
}
