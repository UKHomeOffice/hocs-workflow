package uk.gov.digital.ho.hocs.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.migration.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.migration.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.migration.dto.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@AllArgsConstructor
public class MigrationHelperService {
    final private CamundaClient camundaClient;
    final private CaseworkClient caseworkClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    public void migrate (ProcessExecution execution, int targetDefinitionVersion) throws IOException {

        MigrationPlan migrationPlan = getMigrationPlan(execution, targetDefinitionVersion);

        MigrationPlanValidationResult validationResult = camundaClient.validateMigrationPlan(migrationPlan);

        if (validationResult.getInstructionReports().length == 0){
            camundaClient.executeMigration(migrationPlan, execution.getId());
        } else{
            log.error("Migration plan invalid, migration not executed. Error report: {}",
                    validationResult.getInstructionReports().toString());
        }

    }

    public void fixProcessVariable (ProcessExecution execution, String variableName, String variableType, String variableTrueValue){
        ProcessVariable processVariable =
                camundaClient.getProcessVariable(execution.getId(), variableName);

        if (processVariable.getType().equals("String")
                && !processVariable.getValue().equals(variableTrueValue)){

            camundaClient.updateProcessVariable(
                    execution.getId(), variableName, variableType, variableTrueValue);
        }
    }

    public void fixActiveStageTeam(Stage activeStage, String variableTrueValue){
        if (!activeStage.getTeamUuid().equals(variableTrueValue)) {
            caseworkClient.updateStageTeam(
                    activeStage.getCaseUuid(), activeStage.getUuid(), variableTrueValue);
        }
    }

    public ArrayList<ProcessExecution> getExecutionsByCaseUuid(String caseUuid, Stage activeStage){
        ArrayList<ProcessExecution> executionsList = new ArrayList<>();
        executionsList.addAll(camundaClient.getExecutionsByBusinessKey(caseUuid));
        executionsList.addAll(camundaClient.getExecutionsByBusinessKey(activeStage.getUuid()));
        return executionsList;
    }

    public HashMap<String, ProcessExecution> getExecutionsHashMap (ArrayList<ProcessExecution> executionsList){
        HashMap<String, ProcessExecution> executionHashMap = new HashMap<>();
        for (ProcessExecution execution: executionsList) {
            executionHashMap.put(execution.getDefinitionKey(), execution);
        }
        return executionHashMap;
    }

    private MigrationPlan getMigrationPlan(ProcessExecution execution, int targetDefinitionVersion) throws IOException {
        String[] processDefinitionInfo = execution.getDefinitionId().split(":");
        String processDefinitionKey = processDefinitionInfo[0];
        String processDefinitionVersion = processDefinitionInfo[1];

        String migrationPlanFileName =
                String.format("%s_%s_%d.json", processDefinitionKey, processDefinitionVersion, targetDefinitionVersion);
        log.info("Migration Plan file name: {}", migrationPlanFileName);

        Resource resource = new ClassPathResource("migrationPlans/" + migrationPlanFileName);
        File file = resource.getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, MigrationPlan.class);
    }
}
