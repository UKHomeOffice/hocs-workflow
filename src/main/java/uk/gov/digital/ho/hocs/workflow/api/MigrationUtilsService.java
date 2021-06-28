package uk.gov.digital.ho.hocs.workflow.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaMigrationClient;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.DeploymentDetails;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.ProcessDefinitionCheckSumSummary;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.ProcessDefinitionCheckSumSummary.Meta;

@Service
@Slf4j
public class MigrationUtilsService {

  private final CamundaMigrationClient camundaMigrationClient;

  @Autowired
  public MigrationUtilsService(CamundaMigrationClient camundaMigrationClient) {
    this.camundaMigrationClient = camundaMigrationClient;
  }

  public List<ProcessDefinitionCheckSumSummary> processDefinitionChecksums(String processDefinitionKey, boolean changes) throws IOException {

    List<ProcessInstance> caseExecutions = camundaMigrationClient.calculateCaseExecutionList(processDefinitionKey);
    Map<String, Deployment> deploymentMap = camundaMigrationClient.calculateDeploymentMap();
    SortedMap<String, List<String>> executionKeyMap = camundaMigrationClient.calculateExecutionKeyMap(caseExecutions);
    List<ProcessDefinition> processDefinitionList = camundaMigrationClient.processDefinitionList(processDefinitionKey);

    List<ProcessDefinitionCheckSumSummary> pdsList = new ArrayList<>();
    long oldCheckSum = 0l;
    for (ProcessDefinition pd : processDefinitionList) {
      String pdId = pd.getId();
      int version = pd.getVersion();
      long checkSum = camundaMigrationClient.calculateCheckSumMap(processDefinitionKey).get(pd.getId());
      Deployment deployment = deploymentMap.get(pd.getDeploymentId());
      Meta meta = new Meta(pdId, checkSum);
      DeploymentDetails deploymentDetails = new DeploymentDetails(deployment.getId(), deployment.getName(), deployment.getDeploymentTime(), deployment.getSource(), deployment.getTenantId());
      int caseCount = executionKeyMap.containsKey(pd.getId()) ? executionKeyMap.get(pdId).size() : 0;
      boolean changed = checkSum != oldCheckSum;
      ProcessDefinitionCheckSumSummary pds = new ProcessDefinitionCheckSumSummary(version, caseCount, changed, deploymentDetails, meta);
      if (!changes || pds.isChanged()) {
        pdsList.add(pds);
      }
      oldCheckSum = checkSum;
    }

    return pdsList;
  }
}
