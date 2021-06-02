package uk.gov.digital.ho.hocs.workflow.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.CamundaVariableReport;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationResult;
import uk.gov.digital.ho.hocs.workflow.api.dto.VariableReport;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaMigrationClient;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.CaseExecution;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.CaseTask;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.StageDto;

@Service
@Slf4j
public class MigrationUtilsService {

  private final CamundaMigrationClient camundaMigrationClient;
  private final CaseworkClient caseworkClient;

  @Autowired
  public MigrationUtilsService(CamundaMigrationClient camundaMigrationClient, CaseworkClient caseworkClient) {
    this.camundaMigrationClient = camundaMigrationClient;
    this.caseworkClient = caseworkClient;
  }

  public MigrationResult migrateWithMapEqualActivities(MigrationRequest migrationRequest) {

    String source = migrationRequest.getSource();
    String processDefinitionKey = source.substring(0, source.indexOf(":"));
    Map<String, List<String>> before = camundaMigrationClient.diagramsKey(processDefinitionKey);
    List<String> migratedBusinessKeys = camundaMigrationClient.migrateWithMapEqualActivities(migrationRequest);
    Map<String, List<String>> after = camundaMigrationClient.diagramsKey(processDefinitionKey);
    return new MigrationResult(before, after, migratedBusinessKeys);
  }

  public void setExecutionVariable(UUID executionUuid, String variable, String value) {
    camundaMigrationClient.setExecutionVariable(executionUuid, variable, value);
  }

  public void setTaskVariable(UUID taskUuid, String variable, String value) {
    camundaMigrationClient.setTaskVariable(taskUuid, variable, value);
  }

  public CaseExecution getExecution(UUID executionUuid) {
    return camundaMigrationClient.getExecution(executionUuid);
  }

  public CaseTask getTask(UUID taskUUID) {
    return camundaMigrationClient.getTask(taskUUID);
  }

  public VariableReport report(UUID businessKey) {

    GetCaseworkCaseDataResponse caseData;
    try {
      caseData = caseworkClient.getFullCase(businessKey);
    } catch (Exception e) {
      // this could be a stageUUID
      return reportStage(businessKey);
    }

    List<CaseExecution> caseExecutions = camundaMigrationClient.findExecutionsByBusinessKey(businessKey);
    GetAllStagesForCaseResponse stagesResp = caseworkClient.getAllStagesForCase(businessKey);
    StageDto stage = stagesResp.getStages().stream().filter(StageDto::isActive).findFirst().get();
    List<CaseExecution> stageExecutions = camundaMigrationClient.findExecutionsByBusinessKey(stage.getUuid());

    CaseTask caseTask = camundaMigrationClient.getCaseTaskByStageUuid(stage.getUuid());

    CamundaVariableReport camundaVariableReport = new CamundaVariableReport(caseExecutions, stageExecutions, caseTask);
    return new VariableReport(caseData, stage, camundaVariableReport);
  }

  private VariableReport reportStage(UUID stageUuid) {
    CaseExecution stageExecution = camundaMigrationClient.findExecutionsByBusinessKey(stageUuid).stream().findFirst().get();
    String caseUuid = (String)stageExecution.getVariables().get("CaseUUID");
    return report(UUID.fromString(caseUuid));
  }

  public Map<String, List<String>> diagramsKey(String processDefinitionKey) {
    return camundaMigrationClient.diagramsKey(processDefinitionKey);
  }

  public Map<String, Integer> diagramsCounts(String processDefinitionKey) {
    return camundaMigrationClient.diagramsCounts(processDefinitionKey);
  }

}
