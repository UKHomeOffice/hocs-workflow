package uk.gov.digital.ho.hocs.workflow.camundaClient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

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

    public void startCase(UUID caseUUID, CaseType caseType, Map<String,Object> seedData) {
        log.debug("Starting case bpmn:  Case: '{}' Type: '{}'", caseUUID, caseType);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(caseType.toString(), caseUUID.toString(), seedData);
        log.info("Started case bpmn: Case: '{}' Type: '{}' id: '{}'", caseUUID, caseType, processInstance.getId());
    }

    public String getScreenName(UUID stageUUID) {
        log.debug("Getting current screen for bpmn Stage: '{}'", stageUUID);
        ProcessInstance businessKeyInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(stageUUID.toString())
                .singleResult();

        if (businessKeyInstance != null) {
            String screenName = getValueFromProcess(businessKeyInstance.getProcessInstanceId(), "screen");
            log.info("Got current stage for bpmn Stage: '{}' Screen: '{}'", stageUUID, screenName);
            return screenName;
        } else {
            return "FINISH";
        }
    }

    public void updateStage(UUID stageUUID, Map<String,String> values) {
        log.debug("Validating stage for bpmn Stage: '{}'", stageUUID);

        //TODO: REMOVE THIS DEMO CODE
        values.put("valid", "true");

        Task task = taskService.createTaskQuery().processInstanceBusinessKey(stageUUID.toString()).singleResult();

        if(task != null) {
            taskService.complete(task.getId(), new HashMap<>(values));
            log.info("Validated stage for bpmn Stage: '{}'", stageUUID);
        } else {
            throw new EntityNotFoundException("Failed to validate bpmn Stage: '%s', No tasks returned", stageUUID);
        }
    }

    public void allocateStage(UUID caseUUID, UUID teamUUID, UUID userUUID) {
        log.debug("Allocating Case {} to User {} of Team {}", caseUUID, userUUID, teamUUID);

        Task task = taskService.createTaskQuery().processInstanceBusinessKey(caseUUID.toString()).singleResult();

        if(task != null) {
            taskService.complete(task.getId(), new HashMap<>());
            log.info("Allocated Case {} to User {} of Team {}", caseUUID, userUUID, teamUUID);
        } else {
            throw new EntityNotFoundException("Failed to allocate Case %s, No tasks returned", caseUUID);
        }
    }

    private String getValueFromProcess(String processInstanceId, String key) {
        VariableInstance instance = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstanceId)
                .variableName(key)
                .singleResult();

        if (instance != null) {
            String value = (String) instance.getValue();
            if(value != null) {
                return value;
            } else {
                throw new EntityNotFoundException("Value not found, processInstanceId: %s Key: %s", processInstanceId, key);
            }
        } else {
            throw new EntityNotFoundException("VariableInstance not found, processInstanceId: %s Key: %s", processInstanceId, key);
        }
    }
}
