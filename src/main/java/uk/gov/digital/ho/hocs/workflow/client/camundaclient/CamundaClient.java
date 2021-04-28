package uk.gov.digital.ho.hocs.workflow.client.camundaclient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

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

    public void startCase(UUID caseUUID, String caseDataType, Map<String,String> data) {
        runtimeService.startProcessInstanceByKey(caseDataType, caseUUID.toString(), new HashMap<>(data));
        log.info("Started case bpmn: Case: '{}' Type: '{}'", caseUUID, caseDataType, value(EVENT, CASE_STARTED_SUCCESS));
    }

    public void startCaseWithMessage(UUID caseUUID, String caseDataType, String message, Map<String,String> data) {
        runtimeService.startProcessInstanceByMessage(message, caseUUID.toString(), new HashMap<>(data));
        log.info("Started case bpmn: Case: '{}', Type: '{}', Message: {}, Event {}", caseUUID, caseDataType, message, value(EVENT, CASE_STARTED_SUCCESS));
    }

    /**
     * This currently only supports sequential stages in BPMN diagrams,
     * to support parallel stages we need expect multiple Task objects back from the task service
     * (we can't call .singleResult() like we do now)
     * We then need a way to determine which stage we are allocating (using stageUUID?)
     */
    public void completeTask(UUID key, Map<String,String> data) {
        String taskId = getTaskIdByBusinessKey(key);
        taskService.complete(taskId, new HashMap<>(data));
        log.info("Completed task for key: '{}'", key, value(EVENT, TASK_COMPLETED));
    }

    public void updateTask(UUID key, Map<String,String> data) {
        String taskId = getTaskIdByBusinessKey(key);
        taskService.setVariables(taskId, new HashMap<>(data));
        log.info("Updated task for key: '{}'", key, value(EVENT, TASK_COMPLETED));
    }


    public String getStageScreenName(UUID stageUUID) {
        String formKeyScreenName = getFormKeyForCurrentTask(stageUUID);
        return formKeyScreenName != null ? formKeyScreenName : getStageScreenNameFromProcessVariable(stageUUID);
    }

    public String getFormKeyForCurrentTask(UUID stageUUID) {
        Task task = taskService.createTaskQuery()
            .processInstanceBusinessKey(stageUUID.toString())
            .initializeFormKeys()
            .singleResult();
        return task != null ? task.getFormKey() : null;
    }

    public String getStageScreenNameFromProcessVariable(UUID stageUUID) {
        String screenName = getPropertyByBusinessKey(stageUUID, "screen");
        log.info("Got current stage for bpmn Stage: '{}' Screen: '{}'", stageUUID, screenName, value(EVENT, CURRENT_STAGE_RETRIEVED));
        return screenName == null || screenName.equals("null") ? "FINISH" : screenName;
    }

    private String getTaskIdByBusinessKey(UUID businessKey) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey.toString())
                .singleResult();

        if(task != null) {
            return task.getId();
        } else {
        throw new ApplicationExceptions.EntityNotFoundException(String.format("No tasks returned %s", businessKey), TASK_RETRIEVAL_FAILURE);
        }
    }

    private String getProcessIdByBusinessKey(UUID businessKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey.toString())
                .singleResult();

        if(processInstance != null) {
            return processInstance.getProcessInstanceId();
        } else {
            return null;
        }
    }

    private String getPropertyByBusinessKey(UUID businessKey, String key) {

        String processInstanceId = getProcessIdByBusinessKey(businessKey);

        VariableInstance instance = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstanceId)
                .variableName(key)
                .singleResult();

        if (instance != null) {
            return (String) instance.getValue();
        } else {
            return null;
        }
    }
}
