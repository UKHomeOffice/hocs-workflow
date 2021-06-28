package uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeploymentDetails {

  private final String id;
  private final String name;
  private final Date deploymentTime;
  private final String source;
  private final String tenantId;
}
