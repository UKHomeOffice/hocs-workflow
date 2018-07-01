package uk.gov.digital.ho.hocs.workflow.camundaClient;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class CamundaClient {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Autowired
    public CamundaClient(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    public void startCase(UUID caseUUID, CaseType caseType) throws EntityCreationException {
        log.debug("Starting case bpmn: {} - {}", caseUUID, caseType);
        if (caseUUID != null && caseType != null) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(caseType.toString(), caseUUID.toString(), new HashMap<>());
            log.debug("Started case bpmn: {} - {} id: {}", caseUUID, caseType, processInstance.getCaseInstanceId());
        } else {
            throw new EntityCreationException("Could not start case bpmn, caseUUID or caseType is null!");
        }
    }

    public StageType getCaseStage(UUID caseUUID) throws EntityNotFoundException {
        log.debug("Getting current stage for case bpmn: {}", caseUUID);
        if(caseUUID != null) {
            String stageType = getValueFromProcess(caseUUID, "stage");
            log.debug("Got current stage for case bpmn: {} StageType: {}", caseUUID, stageType);

            return StageType.valueOf(stageType);
        } else {
            throw new EntityNotFoundException("Could not get current stage, caseUUID is null!");
        }
    }

    public String startStage(UUID stageUUID, StageType stageType) throws EntityCreationException, EntityNotFoundException {
        log.debug("Starting stage bpmn: {} - {}", stageUUID, stageType);
        if(stageUUID != null && stageType != null) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(stageType.toString(), stageUUID.toString(), new HashMap<>());
            log.debug("Started stage bpmn: {} - {} id: {}", stageUUID, stageType, processInstance.getCaseInstanceId());

            return getValueFromProcess(processInstance.getProcessInstanceId(), "screen");
        } else {
            throw new EntityCreationException("Could not create case, caseUUID or caseType is null!");
        }
    }

    public String validateStage(UUID stageUUID, Map<String,Object> values) throws EntityNotFoundException {
        log.debug("Validating stage for stage bpmn: {}", stageUUID);
        if(stageUUID != null && !values.isEmpty()) {

            Task task = taskService.createTaskQuery().processInstanceBusinessKey(stageUUID.toString()).singleResult();
            if(task != null) {
                taskService.complete(task.getId(), values);
                log.debug("Validated stage for stage bpmn: {}", stageUUID);
                return getValueFromProcess(task.getProcessInstanceId(), "screen");
            } else {
                log.info("Failed to validate stage bpmn: {}, No tasks returned", stageUUID);
                throw new EntityNotFoundException("Could not get current stage, caseUUID is null!");
            }
        } else {
            throw new EntityNotFoundException("Could not get current stage, caseUUID is null!");
        }
    }

    private String getValueFromProcess(UUID businessKey, String key) throws EntityNotFoundException {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey.toString())
                .singleResult();

        if (instance != null) {
            log.debug("GetValueFromProcess BusinessKey: {} found processInstanceId: {}", businessKey, instance.getCaseInstanceId());
            return getValueFromProcess(instance.getProcessInstanceId(), key);
        } else {
            throw new EntityNotFoundException(String.format("ProcessInstance not found, businessKey: %s'", businessKey));
        }
    }

    private String getValueFromProcess(String processInstanceId, String key) throws EntityNotFoundException {
        VariableInstance instance = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstanceId)
                .variableName(key)
                .singleResult();

        if (instance != null) {
            log.debug("GetValueFromProcess processInstanceId: {} found  value{}", processInstanceId, key);
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
