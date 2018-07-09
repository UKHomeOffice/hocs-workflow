package uk.gov.digital.ho.hocs.workflow.camundaClient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

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

    public StageType startCase(UUID caseUUID, CaseType caseType) throws EntityCreationException, EntityNotFoundException {
        log.debug("Starting case bpmn:  Case: '{}' - '{}'", caseUUID, caseType);
        if (caseUUID != null && caseType != null) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(caseType.toString(), caseUUID.toString(), new HashMap<>());
            log.debug("Started case bpmn: Case: '{}' - '{}' id: '{}'", caseUUID, caseType, processInstance.getId());
            return getCaseStage(caseUUID);
        } else {
            throw new EntityCreationException("Could not start case bpmn, caseUUID or caseType is null!");
        }
    }

    private StageType getCaseStage(UUID caseUUID) throws EntityNotFoundException {
        log.debug("Getting current stage for case bpmn: '{}'", caseUUID);
        if(caseUUID != null) {
            String stageType = getNext(caseUUID, "stage");
            log.debug("Got current stage for case bpmn: '{}' StageType: '{}'", caseUUID, stageType);

            return StageType.valueOf(stageType);
        } else {
            throw new EntityNotFoundException("Could not get current stage, caseUUID is null!");
        }
    }

    //TODO: This is where we assign users
    public void startStage(UUID stageUUID, StageType stageType) throws EntityCreationException, EntityNotFoundException {
        log.debug("Starting stage bpmn: Case:'{}' Stage: '{}'", stageUUID, stageType);
        if(stageUUID != null && stageType != null) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(stageType.toString(), stageUUID.toString(), new HashMap<>());
            log.debug("Started stage bpmn: Case:'{}' Stage: '{}' id: '{}'", stageUUID, stageType, processInstance.getId());
        } else {
            throw new EntityCreationException("Could not create case, caseUUID or caseType is null!");
        }
    }

    public String getCurrentScreen(UUID stageUUID) throws EntityNotFoundException {
        log.debug("Getting current screen for stage bpmn: '{}'", stageUUID);
        if(stageUUID != null) {
            String screenName = getNext(stageUUID, "screen");
            log.debug("Got current stage for stage bpmn: '{}' screen: '{}'", stageUUID, screenName);

            return screenName;
        } else {
            throw new EntityNotFoundException("Could not get current stage, stageUUID is null!");
        }
    }

    public String updateStage(UUID stageUUID, Map<String,Object> values) throws EntityNotFoundException {

        //TODO: REMOVE THIS DEMO CODE
        values.put("valid", true);
        values.put("topic", 8);
        values.put("anotherTopic", false);

        log.debug("Validating stage for stage bpmn: '{}'", stageUUID);
        if(stageUUID != null && !values.isEmpty()) {

            Task task = taskService.createTaskQuery().processInstanceBusinessKey(stageUUID.toString()).singleResult();

            if(task != null) {
                taskService.complete(task.getId(), values);
                log.debug("Validated stage for stage bpmn: '{}'", stageUUID);
                return getNext(task.getProcessInstanceId(), "screen");
            } else {
                throw new EntityNotFoundException("Failed to validate stage bpmn: '{}', No tasks returned", stageUUID);
            }
        } else {
            throw new EntityNotFoundException("Could not get current stage, stageUUID is null or no values!!");
        }
    }

    private String getNext(UUID businessKey, String key) throws EntityNotFoundException {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey.toString())
                .singleResult();

        if (instance != null) {
            log.debug("GetValueFromProcess BusinessKey: '{}' found processInstanceId: '{}'", businessKey, instance.getProcessInstanceId());
            return getNext(instance.getProcessInstanceId(), key);
        } else {
            return "FINISH";
        }
    }

    private String getNext(String processInstanceId, String key) throws EntityNotFoundException {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance != null) {
            log.debug("GetValueFromProcess found processInstanceId: '{}'", processInstanceId);
            return getValueFromProcess(instance.getProcessInstanceId(), key);
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
            log.debug("GetValueFromProcess processInstanceId: '{}' found key: '{}'", processInstanceId, key);
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
