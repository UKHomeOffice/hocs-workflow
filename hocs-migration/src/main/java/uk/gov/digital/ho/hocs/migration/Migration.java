package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.migration.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.migration.client.caseworkclient.CaseworkClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class Migration {
    private CamundaClient camundaClient;
    private CaseworkClient caseworkClient;

    public Migration( CamundaClient camundaClient, CaseworkClient caseworkClient){
        this.camundaClient = camundaClient;
        this.caseworkClient = caseworkClient;
    }

    public void fixCase(String caseUuid) throws IOException {

        String variableName = "CaseworkTeamUUID";
        String variableTrueValue = "f2134fcc-284b-435b-b8cf-247d7397422b";
        String variableType = "String";
        int targetDefinitionVersion = 3;

        String stageUuid = caseworkClient.getActiveStage(caseUuid).getUuid();
        ArrayList<ProcessExecution> executionsList = new ArrayList<ProcessExecution>();
        executionsList.addAll(camundaClient.getExecutionsByBusinessKey(caseUuid));
        executionsList.addAll(camundaClient.getExecutionsByBusinessKey(stageUuid));

        for (ProcessExecution execution: executionsList) {
            if (execution.getDefinitionKey().equals("WCS")){
                migrate(execution, 3);
            }
        }
    }

    private void migrate (ProcessExecution execution, int targetDefinitionVersion) throws IOException {

        MigrationPlan migrationPlan = getMigrationPlan(execution, targetDefinitionVersion);

        MigrationPlanValidationResult validationResult = camundaClient.validateMigrationPlan(migrationPlan);

        if (validationResult.getInstructionReports().length == 0){
            camundaClient.executeMigration(migrationPlan, execution.getId());
        }

    }

    private MigrationPlan getMigrationPlan(ProcessExecution execution, int targetDefinitionId) throws IOException {
        String[] processDefinitionInfo = execution.getDefinitionId().split(":");
        String processDefinitionKey = processDefinitionInfo[0];
        String processDefinitionVersion = processDefinitionInfo[1];

        String migrationPlanFileName =
                processDefinitionKey + "_" + processDefinitionVersion + "_" + targetDefinitionId + ".json";

        Resource resource = new ClassPathResource("migrationPlans/" + migrationPlanFileName);
        File file = resource.getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, MigrationPlan.class);
    }

    private void fixProcessVariable (ProcessExecution execution, String variableName, String variableType, String variableTrueValue){
        ProcessVariable processVariable =
                camundaClient.getProcessVariable(execution.getId(), variableName);

        if (processVariable.getType().equals("String")
                && !processVariable.getValue().equals(variableTrueValue)){

            camundaClient.updateProcessVariable(
                    execution.getId(), variableName, variableType, variableTrueValue);
        }
    }

    private void fixActiveStage(ProcessExecution execution, String variableTrueValue){
        Stage activeStage = caseworkClient.getActiveStage(execution.getBusinessKey());

        if (!activeStage.getTeamUuid().equals(variableTrueValue)) {
            caseworkClient.updateStageTeam(
                    execution.getBusinessKey(), activeStage.getUuid(), variableTrueValue);
        }
    }
}
