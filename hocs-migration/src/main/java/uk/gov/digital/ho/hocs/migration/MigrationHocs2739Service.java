package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.migration.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.migration.client.caseworkclient.CaseworkClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class Migration {
    final private CamundaClient camundaClient;
    final private CaseworkClient caseworkClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void fixCase(String caseUuid) throws IOException {
        log.info("Fixing case {}", caseUuid);

        String variableName = "CaseworkTeamUUID";
        String trueVariableType = "String";
        Stage activeStage = caseworkClient.getActiveStage(caseUuid);

        ArrayList<ProcessExecution> executionsList = getExecutionsByCaseUuid(caseUuid, activeStage);

        HashMap<String, ProcessExecution> executionHashMap = getExecutionsHashMap(executionsList);

        //Find true value for CaseworkTeamUUID (look up caseData for now)
        String trueVariableValue = caseworkClient.getCase(caseUuid).getCaseData().getCaseworkTeamUuid();

        //Find true value for ActiveTeamUUID (look up caseData for now)
        String trueActiveStageTeamUuid = caseworkClient.getCase(caseUuid).getCaseData().getCaseworkTeamUuid();

        for (ProcessExecution execution: executionsList) {
            fixProcessVariable(execution, variableName, trueVariableType, trueVariableValue);
        }

        fixActiveStageTeam(activeStage, trueActiveStageTeamUuid);

        migrate(executionHashMap.get("WCS"), 3);
    }

    private void migrate (ProcessExecution execution, int targetDefinitionVersion) throws IOException {

        MigrationPlan migrationPlan = getMigrationPlan(execution, targetDefinitionVersion);

        MigrationPlanValidationResult validationResult = camundaClient.validateMigrationPlan(migrationPlan);

        if (validationResult.getInstructionReports().length == 0){
            camundaClient.executeMigration(migrationPlan, execution.getId());
        }

    }

    private MigrationPlan getMigrationPlan(ProcessExecution execution, int targetDefinitionVersion) throws IOException {
        String[] processDefinitionInfo = execution.getDefinitionId().split(":");
        String processDefinitionKey = processDefinitionInfo[0];
        String processDefinitionVersion = processDefinitionInfo[1];

        String migrationPlanFileName =
                String.format("%s_%s_%d.json", processDefinitionKey, processDefinitionVersion, targetDefinitionVersion);

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

    private void fixActiveStageTeam(Stage activeStage, String variableTrueValue){
        if (!activeStage.getTeamUuid().equals(variableTrueValue)) {
            caseworkClient.updateStageTeam(
                    activeStage.getCaseUuid(), activeStage.getUuid(), variableTrueValue);
        }
    }

    private ArrayList<ProcessExecution> getExecutionsByCaseUuid(String caseUuid, Stage activeStage){
        ArrayList<ProcessExecution> executionsList = new ArrayList<>();
        executionsList.addAll(camundaClient.getExecutionsByBusinessKey(caseUuid));
        executionsList.addAll(camundaClient.getExecutionsByBusinessKey(activeStage.getUuid()));
        return executionsList;
    }

    private HashMap<String, ProcessExecution> getExecutionsHashMap (ArrayList<ProcessExecution> executionsList){
        HashMap<String, ProcessExecution> executionHashMap = new HashMap<>();
        for (ProcessExecution execution: executionsList) {
            executionHashMap.put(execution.getDefinitionKey(), execution);
        }
        return executionHashMap;
    }

    public List<String> getFixableCases(){
        List<ProcessDefinition> definitionVersions = camundaClient.getProcessDefinitionsByKey("WCS");
        List<ProcessExecution> outdatedExecutions = new ArrayList<>();
        List<String> caseUuids = new ArrayList<>();

        for (int i = 0; i < definitionVersions.size()-1 ; i++) {
            outdatedExecutions.addAll(camundaClient.getExecutionsByWorkflowId(definitionVersions.get(i).getId()));
        }

        for (ProcessExecution execution: outdatedExecutions) {
            caseUuids.add(execution.getBusinessKey());
        }

        return caseUuids;
    }
}
