package uk.gov.digital.ho.hocs.workflow.client.camundaclient;

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

@Service
@Slf4j
public class CamundaMigrationClient {

  private final RuntimeService runtimeService;
  private final TaskService taskService;

  @Autowired
  public CamundaMigrationClient(RuntimeService runtimeService, TaskService taskService) {
    this.runtimeService = runtimeService;
    this.taskService = taskService;
  }

  public List<String> migrateWithMapEqualActivities(MigrationRequest migrationRequest) {

    // ProcessInstanceQuery created which identifies the executions we need to migrate/modify
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery()
        .processDefinitionId(migrationRequest.getSource());
    List<String> businessKeys = processInstanceQuery.list().stream().map(ProcessInstance::getBusinessKey).collect(Collectors.toList());

    // "A migration plan is complete when there are instructions for all active source activities."
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(migrationRequest.getSource(), migrationRequest.getTarget())
        .mapEqualActivities()
        .build();

    //Execute this plan using the ProcessInstanceQuery
    if (migrationRequest.getAction() != null && migrationRequest.getAction().equals("PERFORM_MIGRATION"))
    runtimeService.newMigration(migrationPlan)
        .processInstanceQuery(processInstanceQuery)
        .execute();

    return businessKeys;
  }

  public CaseExecution getExecution(UUID executionUuid) {

    ProcessInstance execution = runtimeService.createProcessInstanceQuery()
        .processInstanceId(executionUuid.toString())
        .singleResult();

    return new CaseExecution(
        execution.getProcessDefinitionId(),
        execution.getBusinessKey(),
        execution.getProcessInstanceId(),
        execution.getId(),
        runtimeService.getVariables(execution.getId())
    );
  }

  public List<CaseExecution> findExecutionsByBusinessKey(UUID businessKey) {

    return runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey.toString())
        .list()
        .stream()
        .map(pi -> getExecution(UUID.fromString(pi.getId())))
        .collect(Collectors.toList());
  }

  public CaseTask getCaseTaskByStageUuid(UUID stageUUID) {
    Task task = taskService.createTaskQuery().initializeFormKeys().processInstanceBusinessKeyIn(stageUUID.toString()).singleResult();
    Map<String, Object> taskVariables = taskService.getVariables(task.getId());
    return new CaseTask(task.getId(), task.getTaskDefinitionKey(), task.getName(), task.getFormKey(), task.getProcessDefinitionId(), taskVariables);
  }

  public CaseTask getTask(UUID taskUUID) {
    Task task = taskService.createTaskQuery().initializeFormKeys().taskId(taskUUID.toString()).singleResult();
    Map<String, Object> taskVariables = taskService.getVariables(task.getId());
    return new CaseTask(task.getId(), task.getTaskDefinitionKey(), task.getName(), task.getFormKey(), task.getProcessDefinitionId(), taskVariables);
  }

  public Map<String, List<String>> diagramsKey(String processDefinitionKey) {
    return getMap(processDefinitionKey);
  }

  public Map<String, Integer> diagramsCounts(String processDefinitionKey) {

    SortedMap<String, List<String>> executionKeyMap = getMap(processDefinitionKey);

    SortedMap<String, Integer> executionCounts = new TreeMap<>();
    for (String key : executionKeyMap.keySet()) {
      executionCounts.put(key, executionKeyMap.get(key).size());
    }

    return executionCounts;
  }

  private SortedMap<String, List<String>> getMap(String processDefinitionKey) {

    //Actual executions are being returned here. Can get more than one for a single business key
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
      } else {
        keyMap.add(processInstance.getBusinessKey());
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

    private final String id;
    private final String definitionKey;
    private final String name;
    private final String formKey;
    private final String processDefinitionId;
    private final Map<String, Object> variables;
  }
}
