package uk.gov.digital.ho.hocs.migration.client.camundaclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.migration.dto.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CamundaClient {
    private final RestTemplate restTemplate;
    private final String serviceBaseURL;

    public CamundaClient(RestTemplate restTemplate, @Value("${hocs.camunda-service.url}") String camundaUrl) {
        this.restTemplate = restTemplate;
        this.serviceBaseURL=camundaUrl;
    }

    public List<ProcessExecution> getExecutionsByWorkflowId(String processDefinitionId) {
        ProcessExecution[] executions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance?processDefinitionId=" + processDefinitionId,
                ProcessExecution[].class);
        
        return Arrays.asList(executions);
    }

    public List<ProcessDefinition> getProcessDefinitionsByKey(String processDefinitionKey){
        ProcessDefinition[] processDefinitions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-definition?key=" + processDefinitionKey + "&sortBy=version&sortOrder=asc",
                ProcessDefinition[].class);

        return Arrays.asList(processDefinitions);
    }

    public List<ProcessExecution> getExecutionsByBusinessKey(String businessKey){
        ProcessExecution[] executions = restTemplate.getForObject(
                serviceBaseURL +"/rest/process-instance?businessKey=" + businessKey,
                ProcessExecution[].class);
        return Arrays.asList(executions);

    };

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
        UpdateProcessVariableRequest requestBody = new UpdateProcessVariableRequest(variableType, variableValue, null);

        restTemplate.put(serviceBaseURL +"/rest/process-instance/" + executionId + "/variables/" + variableName,
                requestBody);

        log.info("Updated process variable");
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

        MigrationExecutionRequest executionRequest = new MigrationExecutionRequest(migrationPlan, executionIds);

        ResponseEntity response = restTemplate.postForEntity(serviceBaseURL +"/rest/migration/execute",
                executionRequest, ResponseEntity.class);

    }

}
