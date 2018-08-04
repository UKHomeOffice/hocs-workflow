package uk.gov.digital.ho.hocs.workflow.camundaClient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
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

    public void startCase(UUID caseUUID, CaseType caseType) {
        log.debug("Starting case bpmn:  Case: '{}' Type: '{}'", caseUUID, caseType);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(caseType.toString(), caseUUID.toString(), new HashMap<>());
        log.info("Started case bpmn: Case: '{}' Type: '{}' id: '{}'", caseUUID, caseType, processInstance.getId());
    }

    public String getScreenName(UUID stageUUID) {
        log.debug("Getting current screen for bpmn Stage: '{}'", stageUUID);
        String screenName = getVariableName(stageUUID, "screen");
        log.info("Got current stage for bpmn Stage: '{}' Screen: '{}'", stageUUID, screenName);
        return screenName;
    }

    public void updateStage(UUID stageUUID, Map<String,String> values) throws EntityNotFoundException {

        //TODO: REMOVE THIS DEMO CODE
        values.put("valid", "true");

        log.debug("Validating stage for bpmn Stage: '{}'", stageUUID);
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(stageUUID.toString()).singleResult();

        if(task != null) {
            taskService.complete(task.getId(), new HashMap<>(values));
            log.info("Validated stage for bpmn Stage: '{}'", stageUUID);
        } else {
            throw new EntityNotFoundException("Failed to validate bpmn Stage: '%s', No tasks returned", stageUUID);
        }
    }

    private String getVariableName(UUID businessKey, String key) throws EntityNotFoundException {
        ProcessInstance businessKeyInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey.toString())
                .singleResult();

        if (businessKeyInstance != null) {
            return getValueFromProcess(businessKeyInstance.getProcessInstanceId(), key);
        } else {
            return "FINISH";
        }
    }

    private String getValueFromProcess(String processInstanceId, String key) throws EntityNotFoundException {
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
