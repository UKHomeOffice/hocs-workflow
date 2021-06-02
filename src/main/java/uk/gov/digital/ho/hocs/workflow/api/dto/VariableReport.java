package uk.gov.digital.ho.hocs.workflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

@AllArgsConstructor()
@Getter
public class VariableReport {

  private final String caseUuid;
  private final String stageUuid;
  private final GetCaseworkCaseDataResponse caseData;
  private final CamundaVariableReport camunda;



}
