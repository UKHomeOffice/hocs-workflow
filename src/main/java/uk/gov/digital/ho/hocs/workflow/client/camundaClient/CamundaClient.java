package uk.gov.digital.ho.hocs.workflow.client.camundaClient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public void startCase(UUID caseUUID, CaseDataType caseDataType, Map<String,String> data) {
        runtimeService.startProcessInstanceByKey(caseDataType.toString(), caseUUID.toString(), new HashMap<>(data));
        log.info("Started case bpmn: Case: '{}' Type: '{}'", caseUUID, caseDataType);
    }

    /**
     * This currently only supports sequential stages in BPMN diagrams,
     * to support parallel stages we need expect multiple Task objects back from the task service
     * (we can't call .singleResult() like we do now)
     * We then need a way to determine which stage we are allocating (using stageUUID?)
     */
    public void allocateStage(UUID caseUUID, UUID teamUUID, UUID userUUID) {
        String taskId = getTaskIdByBusinessKey(caseUUID);
        taskService.complete(taskId, new HashMap<>());
        log.info("Allocated Case {} to User {} of Team {}", caseUUID, userUUID, teamUUID);
    }

    public String getStageScreenName(UUID stageUUID) {
        String screenName = getPropertyByBusinessKey(stageUUID, "screen");
        log.info("Got current stage for bpmn Stage: '{}' Screen: '{}'", stageUUID, screenName);
        return screenName == null ? "FINISH" : screenName;
    }

    public void completeStage(UUID stageUUID, Map<String,String> data) {
        String taskId = getTaskIdByBusinessKey(stageUUID);
        taskService.complete(taskId, new HashMap<>(data));
        log.info("Validated stage for bpmn Stage: '{}'", stageUUID);
    }

    private String getTaskIdByBusinessKey(UUID businessKey) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey.toString())
                .singleResult();

        if(task != null) {
            return task.getId();
        } else {
        throw new EntityNotFoundException("No tasks returned", businessKey);
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
            throw new EntityNotFoundException("VariableInstance not found, processInstanceId: %s Key: %s", processInstanceId, key);
        }
    }
}
