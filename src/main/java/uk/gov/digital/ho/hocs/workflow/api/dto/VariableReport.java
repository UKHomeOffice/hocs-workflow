package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.StageDto;

@AllArgsConstructor()
@Getter
@JsonPropertyOrder({"case", "stage","camunda" })
public class VariableReport {

  @JsonProperty("case")
  private final GetCaseworkCaseDataResponse caseData;
  private final StageDto stage;
  private final CamundaVariableReport camunda;



}
