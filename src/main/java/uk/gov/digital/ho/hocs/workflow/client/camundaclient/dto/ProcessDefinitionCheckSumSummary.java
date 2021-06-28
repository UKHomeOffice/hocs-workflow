package uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProcessDefinitionCheckSumSummary {

  private int version;
  private final int cases;
  private final boolean changed;
  private final DeploymentDetails deployment;
  private final Meta meta;

  @AllArgsConstructor
  @Getter
  public static class Meta {
    private final String id;
    private final long checkSum;
  }

}

