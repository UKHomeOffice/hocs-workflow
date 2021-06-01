package uk.gov.digital.ho.hocs.workflow.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetAllStagesForCaseResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;

@Service
@Slf4j
public class MigrationToolsService {

  private final RuntimeService runtimeService;
  private final TaskService taskService;
  private final CaseworkClient caseworkClient;

  @Autowired
  public MigrationToolsService(RuntimeService runtimeService, TaskService taskService, CaseworkClient caseworkClient) {
    this.runtimeService = runtimeService;
    this.taskService = taskService;
    this.caseworkClient = caseworkClient;
  }

  public List<String> migrate(MigrationRequest migrationRequest) {

    //SHOW COUNTS AND BUSINESS KEYS FOR TARGET SOURCE AND PROC DEFINITION

    // ProcessInstanceQuery created which identifies the executions we need to migrate/modify
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery()
        .processDefinitionId(migrationRequest.getSource());
    List<String> businessKeys = processInstanceQuery.list().stream().map(ProcessInstance::getBusinessKey).collect(Collectors.toList());

    // "A migration plan is complete when there are instructions for all active source activities."
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(migrationRequest.getSource(), migrationRequest.getTarget())
        .mapEqualActivities()
        .build();

    //Execute this plan using the ProcessInstanceQuery
    runtimeService.newMigration(migrationPlan)
        .processInstanceQuery(processInstanceQuery)
        .execute();

    //SHOW COUNTS AND BUSINESS KEYS FOR TARGET PROC DEFINITION

    return businessKeys;
  }

  public CaseExecution getExecution(UUID executionUuid) {

    ProcessInstance execution = runtimeService.createProcessInstanceQuery()
        .processInstanceId(executionUuid.toString())
        .singleResult();

    CaseExecution caseExecution = new CaseExecution(
        execution.getProcessDefinitionId(),
        execution.getBusinessKey(),
        execution.getProcessInstanceId(),
        execution.getId(),
        runtimeService.getVariables(execution.getId())
    );

    return caseExecution;
  }

  public MigrationCompare report(UUID caseUuid) {

    GetCaseworkCaseDataResponse caseData = null;
    try {
      caseData = caseworkClient.getFullCase(caseUuid);
      System.out.println("Case found with that UUID");
    } catch (Exception e) {
      System.out.println("No case found with that UUID");
      return reportStage(caseUuid);
    }

    GetAllStagesForCaseResponse stagesResp = caseworkClient.getAllStagesForCase(caseUuid);
    UUID stageUuid = stagesResp.getStages().get(0).getUuid();

    Task task = taskService.createTaskQuery().processInstanceBusinessKeyIn(stageUuid.toString()).singleResult();
    CaseTask caseTask = new CaseTask(task.getTaskDefinitionKey(), task.getName(), task.getProcessDefinitionId());

    //Actually executions are being returned here. Can get more than one for a single business key
    List<ProcessInstance> caseExecutionsList = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(caseUuid.toString())
        .list();

    List<ProcessInstance> stageExecutions = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(stageUuid.toString())
        .list();

    List<ProcessInstance> executions = new ArrayList<>();
    executions.addAll(caseExecutionsList);
    executions.addAll(stageExecutions);

    //lower levels have the STAGE id as the business key FFF.

    List<CaseExecution> caseExecutions = new ArrayList<>();
    for (ProcessInstance execution : executions) {
      CaseExecution caseExecution = new CaseExecution(
          execution.getProcessDefinitionId(),
          execution.getBusinessKey(),
          execution.getProcessInstanceId(),
          execution.getId(),
          runtimeService.getVariables(execution.getId())
      );
      caseExecutions.add(caseExecution);
    }

    MigrationCompare migrationCompare = new MigrationCompare(caseData, new Camunda(caseExecutions, caseTask));

    return migrationCompare;
  }

  public Map<String, List<String>> diagramsKey(String processDefinitionKey) {

    Map<String, List<String>> executionKeyMap = getMap(processDefinitionKey);

    return executionKeyMap;
  }

  public Map<String, Integer> diagramsCounts(String processDefinitionKey) {

    SortedMap<String, List<String>> executionKeyMap = getMap(processDefinitionKey);

    SortedMap<String, Integer> executionCounts = new TreeMap<>();
    for (String key : executionKeyMap.keySet()) {
      executionCounts.put(key, executionKeyMap.get(key).size());
    }

    return executionCounts;
  }

  private MigrationCompare reportStage(UUID stageUuid) {

    List<ProcessInstance> stageExecutions = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(stageUuid.toString())
        .list();

    ProcessInstance execution = stageExecutions.get(0);
    String caseUuid = (String)runtimeService.getVariable(execution.getId(), "CaseUUID");
    return report(UUID.fromString(caseUuid));
  }

  private SortedMap<String, List<String>> getMap(String processDefinitionKey) {

    //Actually executions are being returned here. Can get more than one for a single business key
    List<ProcessInstance> caseExecutionsList = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();

    SortedMap<String, List<String>> executionKeyMap = new TreeMap<>();
    for (ProcessInstance processInstance : caseExecutionsList) {
      String definitionId = processInstance.getProcessDefinitionId();
      List<String> keyMap = executionKeyMap.get(definitionId);
      if (keyMap == null) {
        List<String> newList = new ArrayList<>();
        executionKeyMap.put(definitionId, newList);
        newList.add(processInstance.getBusinessKey());
      }
    }

    return executionKeyMap;
  }

  @AllArgsConstructor()
  @Getter
  public class CaseExecution {

    private final String processDefinitionId;
    private final String businessKey;
    private final String processInstanceId;
    private final String executionId;
    private final Map<String, Object> variables;
  }

  @AllArgsConstructor()
  @Getter
  public class CaseTask {

    private final String definitionKey;
    private final String name;
    private final String processDefinitionId;
  }

  @AllArgsConstructor()
  @Getter
  public class Camunda {

    private final List<CaseExecution> executions;
    private final CaseTask task;
  }

  @AllArgsConstructor()
  @Getter
  public class MigrationCompare {

    private final GetCaseworkCaseDataResponse caseData;
    private final Camunda camunda;
  }

}
