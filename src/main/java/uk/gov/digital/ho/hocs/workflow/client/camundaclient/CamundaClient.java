package uk.gov.digital.ho.hocs.workflow.client.camundaclient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.ProcessVariables;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CASE_STARTED_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CURRENT_STAGE_RETRIEVED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.TASK_COMPLETED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.TASK_RETRIEVAL_FAILURE;

@Slf4j
@Component
public class CamundaClient {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    @Autowired
    public CamundaClient(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    public void startCase(UUID caseUUID, String caseDataType, Map<String, String> data) {
        runtimeService.startProcessInstanceByKey(caseDataType, caseUUID.toString(), new HashMap<>(data));
        log.info("Started case bpmn: Case: '{}' Type: '{}'", caseUUID, caseDataType,
            value(EVENT, CASE_STARTED_SUCCESS));
    }

    public void startCaseWithMessage(UUID caseUUID, String caseDataType, String message, Map<String, String> data) {
        runtimeService.startProcessInstanceByMessage(message, caseUUID.toString(), new HashMap<>(data));
        log.info("Started case bpmn: Case: '{}', Type: '{}', Message: {}, Event {}", caseUUID, caseDataType, message,
            value(EVENT, CASE_STARTED_SUCCESS));
    }

    public void migrateCases(List<UUID> caseUUIDs, String sourceVersion, String targetVersion) {
        MigrationPlan migrationPlan = runtimeService
                    .createMigrationPlan(sourceVersion, targetVersion)
                    .mapEqualActivities()
                    .build();

        ProcessInstanceQuery processInstanceQuery = runtimeService
            .createProcessInstanceQuery()
            .processDefinitionId(sourceVersion);

        List<String> processInstanceIDs = caseUUIDs.stream().map(caseUUID -> processInstanceQuery.processInstanceBusinessKey(caseUUID.toString()).list().get(0).getProcessInstanceId()).collect(
            Collectors.toList());

        runtimeService
            .newMigration(migrationPlan)
            .processInstanceIds(processInstanceIDs)
            .execute();
    }

    /**
     * This currently only supports sequential stages in BPMN diagrams,
     * to support parallel stages we need expect multiple Task objects back from the task service
     * (we can't call .singleResult() like we do now)
     * We then need a way to determine which stage we are allocating (using stageUUID?)
     */
    public void completeTask(UUID key, Map<String, String> data) {
        String taskId = getTaskIdByBusinessKey(key);
        taskService.complete(taskId, new HashMap<>(data));
        log.info("Completed task for key: '{}'", key, value(EVENT, TASK_COMPLETED));
    }

    public void updateTask(UUID key, Map<String, String> data) {
        String taskId = getTaskIdByBusinessKey(key);
        taskService.setVariables(taskId, new HashMap<>(data));
        log.info("Updated task for key: '{}'", key, value(EVENT, TASK_COMPLETED));
    }

    public void removeTaskVariables(UUID key, String... keys) {
        String taskId = getTaskIdByBusinessKey(key);
        taskService.removeVariables(taskId, List.of(keys));
        log.info("Removed task for key: '{}'", key, value(EVENT, TASK_COMPLETED));
    }

    public String getStageScreenName(UUID stageUUID) {
        String formKeyScreenName = getFormKeyForCurrentTask(stageUUID);
        return formKeyScreenName != null ? formKeyScreenName : getStageScreenNameFromProcessVariable(stageUUID);
    }

    public String getFormKeyForCurrentTask(UUID stageUUID) {
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(
            stageUUID.toString()).initializeFormKeys().singleResult();
        return task != null ? task.getFormKey() : null;
    }

    public String getStageScreenNameFromProcessVariable(UUID stageUUID) {
        String screenName = getPropertyByBusinessKey(stageUUID, "screen");
        log.info("Got current stage for bpmn Stage: '{}' Screen: '{}'", stageUUID, screenName,
            value(EVENT, CURRENT_STAGE_RETRIEVED));
        return screenName == null || screenName.equals("null") ? "FINISH" : screenName;
    }

    public boolean hasProcessInstanceVariableWithValue(String businessKey, String variableName, String value) {
        List<ProcessInstance> matches = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(
            businessKey).variableValueEquals(variableName, value).list();
        return !matches.isEmpty();
    }

    public void removeProcessInstanceVariableFromAllScopes(String caseUUID, String stageUUID, String variableName) {
        List<ProcessInstance> caseProcessInstanceList = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(
            caseUUID).list();
        caseProcessInstanceList.forEach(pI -> runtimeService.removeVariable(pI.getProcessInstanceId(), variableName));

        List<ProcessInstance> stageProcessInstanceList = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(
            stageUUID).list();
        stageProcessInstanceList.forEach(pI -> runtimeService.removeVariable(pI.getProcessInstanceId(), variableName));
    }

    private String getTaskIdByBusinessKey(UUID businessKey) {
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey.toString()).singleResult();

        if (task != null) {
            return task.getId();
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("No tasks returned %s", businessKey),
                TASK_RETRIEVAL_FAILURE);
        }
    }

    private String getProcessIdByBusinessKey(UUID businessKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(
            businessKey.toString()).singleResult();

        if (processInstance != null) {
            return processInstance.getProcessInstanceId();
        } else {
            return null;
        }
    }

    private String getPropertyByBusinessKey(UUID businessKey, String key) {

        String processInstanceId = getProcessIdByBusinessKey(businessKey);

        VariableInstance instance = runtimeService.createVariableInstanceQuery().processInstanceIdIn(
            processInstanceId).variableName(key).singleResult();

        if (instance != null) {
            return (String) instance.getValue();
        } else {
            return null;
        }
    }

    public void removeProcess(UUID stageUUID) {
        String processID = getProcessIdByBusinessKey(stageUUID);
        runtimeService.deleteProcessInstance(processID, null);
    }

    public List<ProcessVariables> getProcessVariablesForCase(UUID caseUUID, UUID stageUUID) {
        return Stream.of(caseUUID, stageUUID)
                     .filter(Objects::nonNull)
                     .map(Objects::toString)
                     .flatMap(this::streamProcessInstancesForBusinessKey)
                     .map(this::getProcessVariables)
                     .toList();
    }

    public ProcessVariables getProcessVariablesForInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return getProcessVariables(instance);
    }

    private Stream<ProcessInstance> streamProcessInstancesForBusinessKey(String key) {
        return runtimeService
            .createProcessInstanceQuery()
            .processInstanceBusinessKey(key)
            .list().stream();
    }

    private ProcessVariables getProcessVariables(ProcessInstance instance) {
        return new ProcessVariables(
            instance.getProcessInstanceId(),
            instance.getBusinessKey(),
            instance.getRootProcessInstanceId(),
            runtimeService
                .getVariables(instance.getProcessInstanceId())
                .entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Optional.ofNullable(entry.getValue()).map(Objects::toString)
                    )
                )
        );
    }

    public void updateProcessVariables(String processInstanceId, Map<String, ?> processVariables) {
        runtimeService.setVariables(processInstanceId, processVariables);
    }
}
