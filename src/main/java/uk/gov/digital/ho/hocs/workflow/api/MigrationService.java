package uk.gov.digital.ho.hocs.workflow.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationRequest;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaMigrationClient;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaMigrationClient.CaseExecution;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaMigrationClient.CaseTask;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

@Service
@Slf4j
public class MigrationService {

  private final CamundaMigrationClient camundaMigrationClient;
  private final CaseworkClient caseworkClient;

  @Autowired
  public MigrationService(CamundaMigrationClient camundaMigrationClient, CaseworkClient caseworkClient) {
    this.camundaMigrationClient = camundaMigrationClient;
    this.caseworkClient = caseworkClient;
  }

  public MigrationResult migrate(MigrationRequest migrationRequest) {

    String source = migrationRequest.getSource();
    String processDefinitionKey = source.substring(0, source.indexOf(":"));
    Map<String, List<String>> before = camundaMigrationClient.diagramsKey(processDefinitionKey);
    List<String> migratedBusinessKeys = camundaMigrationClient.migrate(migrationRequest);
    Map<String, List<String>> after = camundaMigrationClient.diagramsKey(processDefinitionKey);
    return new MigrationResult(before, after, migratedBusinessKeys);
  }

  public CaseExecution getExecution(UUID executionUuid) {
    return camundaMigrationClient.getExecution(executionUuid);
  }

  public CaseTask getTask(UUID taskUUID) {
    return camundaMigrationClient.getTask(taskUUID);
  }

  public Report report(UUID caseUuid) {

    try {
      caseworkClient.getCase(caseUuid);
    } catch (Exception e) {
      // possibly this could be a stageUUID
      return reportStage(caseUuid);
    }

    List<CaseExecution> caseExecutions = camundaMigrationClient.findExecutionsByBusinessKey(caseUuid);
    GetAllStagesForCaseResponse stagesResp = caseworkClient.getAllStagesForCase(caseUuid);
    UUID stageUuid = stagesResp.getStages().get(0).getUuid();
    List<CaseExecution> stageExecutions = camundaMigrationClient.findExecutionsByBusinessKey(stageUuid);

    CaseTask caseTask = camundaMigrationClient.getCaseTaskByStageUuid(stageUuid);

    return new Report(caseUuid.toString(), stageUuid.toString(), caseExecutions, stageExecutions, caseTask);
  }

  private Report reportStage(UUID stageUuid) {
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

  @AllArgsConstructor()
  @Getter
  public class Report {

    private final String caseUuid;
    private final String stageUuid;
    private final List<CaseExecution> caseExecutions;
    private final List<CaseExecution> stageExecutions;
    private final CaseTask task;
  }

  @AllArgsConstructor()
  @Getter
  public class MigrationResult {

    private final Map<String, List<String>> before;
    private final Map<String, List<String>> after;
    private final List<String> migrated;
  }

}
