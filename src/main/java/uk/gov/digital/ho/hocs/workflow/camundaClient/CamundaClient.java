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

    public void startCase(UUID caseUUID, CaseType caseType) throws EntityCreationException, EntityNotFoundException {
        log.info("Starting case bpmn:  Case: '{}' Type: '{}'", caseUUID, caseType);
        if (caseUUID != null && caseType != null) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(caseType.toString(), caseUUID.toString(), new HashMap<>());
            log.debug("Started case bpmn: Case: '{}' Type: '{}' id: '{}'", caseUUID, caseType, processInstance.getId());
        } else {
            throw new EntityCreationException("Could not start case bpmn, caseUUID or caseType is null!");
        }
    }

    private StageType getCaseStage(UUID caseUUID) throws EntityNotFoundException {
        log.info("Getting current stage for bpmn Case:'{}'", caseUUID);
        if(caseUUID != null) {
            String stageType = getNext(caseUUID, "stage");
            log.debug("Got current stage ({}) for  bpmn Case: '{}' StageType: '{}'", stageType, caseUUID);

            return StageType.valueOf(stageType);
        } else {
            throw new EntityNotFoundException("Could not get current stage, caseUUID is null!");
        }
    }

    public void startStage(UUID stageUUID, StageType stageType) throws EntityCreationException, EntityNotFoundException {
        log.info("Starting stage bpmn Stage:'{}' Type: '{}'", stageUUID, stageType);
        if(stageUUID != null && stageType != null) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(stageType.toString(), stageUUID.toString(), new HashMap<>());
            log.debug("Started stage bpmn Stage:'{}' Type: '{}' id: '{}'", stageUUID, stageType, processInstance.getId());
        } else {
            throw new EntityCreationException("Could not create case, caseUUID or caseType is null!");
        }
    }

    public String getCurrentScreen(UUID stageUUID) throws EntityNotFoundException {
        log.info("Getting current screen for bpmn Stage: '{}'", stageUUID);
        if(stageUUID != null) {
            String screenName = getNext(stageUUID, "screen");
            log.debug("Got current stage for bpmn Stage: '{}' Screen: '{}'", stageUUID, screenName);
            return screenName;
        } else {
            throw new EntityNotFoundException("Could not get current stage, stageUUID is null!");
        }
    }

    public String updateStage(UUID stageUUID, Map<String,String> values) throws EntityNotFoundException {

        //TODO: REMOVE THIS DEMO CODE
        values.put("valid", "true");

        log.info("Validating stage for bpmn Stage: '{}'", stageUUID);
        if(stageUUID != null && !values.isEmpty()) {

            Task task = taskService.createTaskQuery().processInstanceBusinessKey(stageUUID.toString()).singleResult();

            if(task != null) {
                Map<String, Object> objectHashMap = new HashMap<>(values);
                taskService.complete(task.getId(), objectHashMap);
                log.debug("Validated stage for bpmn Stage: '{}'", stageUUID);
                return getNext(task.getProcessInstanceId(), "screen");
            } else {
                throw new EntityNotFoundException("Failed to validate bpmn Stage: '%s', No tasks returned", stageUUID);
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
            return getValueFromProcess(instance.getProcessInstanceId(), key);
        } else {
            return "FINISH";
        }
    }

    public String getValueFromProcess(String processInstanceId, String key) throws EntityNotFoundException {
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
