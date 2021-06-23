package uk.gov.digital.ho.hocs.migration.client.camundaclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.migration.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CamundaClient {
    private final RestTemplate restTemplate;
    private final String serviceBaseURL;

    public CamundaClient(RestTemplate restTemplate, @Value("${hocs.camunda-service}") String camundaUrl) {
        this.restTemplate = restTemplate;
        this.serviceBaseURL=camundaUrl;
    }

    public List<ProcessExecution> getExecutionsByWorkflowId(String processDefinitionId) {
        ProcessExecution[] executions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance?processDefinitionId=" + processDefinitionId,
                ProcessExecution[].class);

        if (executions != null && executions.length > 0) {
            log.info("Executions on workflow: {}", Arrays.toString(executions));
        } else {
            log.info("No executions found on specified workflow");
        }

        return Arrays.asList(executions);
    }

    public List<ProcessExecution> getExecutionsByBusinessKey(String businessKey){
        ProcessExecution[] executions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance?businessKey=" + businessKey,
                ProcessExecution[].class);
        return Arrays.asList(executions);

    };

    public List<ProcessExecution> getExecutionsByCaseUuid(String caseUuid) {
        ProcessExecution[] executions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance?variables=CaseUUID_eq_" + caseUuid,
                ProcessExecution[].class);

        if (executions != null && executions.length > 0) {
            log.info("Executions for case: {}", Arrays.toString(executions));
        } else {
            log.info("No executions found for case uuid");
        }

        return Arrays.asList(executions);
    }

    public ProcessVariable getProcessVariable(String executionId, String variableName) {
        ProcessVariable processVariable = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance/" + executionId + "/variables/" + variableName,
                ProcessVariable.class);

        if (processVariable != null) {
            log.info("Process variable {}: {}", variableName, processVariable.getValue());
        }

        return processVariable;
    }

    public void updateProcessVariable(String executionId, String variableName, String variableType, String variableValue) {
        UpdateProcessVariableRequest requestBody = new UpdateProcessVariableRequest();
        requestBody.setType(variableType);
        requestBody.setValue(variableValue);

        restTemplate.put(serviceBaseURL +"/rest/process-instance/" + executionId + "/variables/" + variableName,
                requestBody);

        log.info("Updated process variable");
    }

    public MigrationPlan generateMigrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
        GenerateMigrationPlanRequest requestBody = new GenerateMigrationPlanRequest();
        requestBody.setSourceProcessDefinitionId(sourceProcessDefinitionId);
        requestBody.setTargetProcessDefinitionId(targetProcessDefinitionId);

        MigrationPlan migrationPlan = restTemplate.postForObject(serviceBaseURL +"/rest/migration/generate",
                requestBody, MigrationPlan.class);

        if (migrationPlan != null) {
            log.info("Migration plan generated for {} -> {}", sourceProcessDefinitionId, targetProcessDefinitionId);
        }

        return migrationPlan;
    }

    public MigrationPlanValidationResult validateMigrationPlan(MigrationPlan migrationPlan) {
        MigrationPlanValidationResult validationResult = restTemplate.postForObject(serviceBaseURL +"/rest/migration/validate",
                migrationPlan, MigrationPlanValidationResult.class);

        if (validationResult != null && validationResult.getInstructionReports().length == 0) {
            log.info("Migration plan valid");
        } else {
            log.info("Migration plan invalid");
        }
        return validationResult;
    }

    public void executeMigration(MigrationPlan migrationPlan, String executionId){
        String[] executionIds = new String[1];
        executionIds[0] = executionId;

        MigrationExecutionRequest executionRequest = new MigrationExecutionRequest();
        executionRequest.setMigrationPlan(migrationPlan);
        executionRequest.setProcessInstanceIds(executionIds);

        ResponseEntity response = restTemplate.postForEntity(serviceBaseURL +"/rest/migration/execute",
                executionRequest, ResponseEntity.class);

        if (response.getStatusCodeValue() == 204){
            log.info("Migration executed successfully");
        } else {
            log.error("Migration could not be executed. {}", response.getBody().toString());
        }
    }

    public ProcessExecution getExecutionByBusinessKeyAndProcessDefinitionKey(String businessKey, String processDefinitionKey) {

        ProcessExecution[] executions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance?businessKey=" + businessKey + "&processDefinitionKey=" + processDefinitionKey,
                ProcessExecution[].class);

        if (executions != null && executions.length == 1){
            return executions[0];
        } else {
            log.error("More than one execution found on workflow.");
            throw new RuntimeException();
        }
    }
}
