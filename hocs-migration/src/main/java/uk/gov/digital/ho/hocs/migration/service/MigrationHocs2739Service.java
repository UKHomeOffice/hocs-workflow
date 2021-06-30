package uk.gov.digital.ho.hocs.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.migration.MigrationService;
import uk.gov.digital.ho.hocs.migration.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.migration.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.migration.dto.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class MigrationHocs2739Service implements MigrationService {
    final private CamundaClient camundaClient;
    final private CaseworkClient caseworkClient;
    final private MigrationHelperService helperService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getDescription() {
        return "Service to fix HOCS-2739";
    }
    
    @Override
    public void fixCase(String caseUuid) throws IOException {
        log.info("Fixing case {}", caseUuid);

        String variableName = "CaseworkTeamUUID";
        String trueVariableType = "String";
        Stage activeStage = caseworkClient.getActiveStage(caseUuid);

        ArrayList<ProcessExecution> executionsList = helperService.getExecutionsByCaseUuid(caseUuid, activeStage);

        HashMap<String, ProcessExecution> executionHashMap = helperService.getExecutionsHashMap(executionsList);

        //Find true value for CaseworkTeamUUID (look up caseData for now)
        String trueVariableValue = caseworkClient.getCase(caseUuid).getCaseData().getCaseworkTeamUuid();

        //Find true value for ActiveTeamUUID (look up caseData for now)
        String trueActiveStageTeamUuid = caseworkClient.getCase(caseUuid).getCaseData().getCaseworkTeamUuid();

        for (ProcessExecution execution: executionsList) {
            helperService.fixProcessVariable(execution, variableName, trueVariableType, trueVariableValue);
        }

        helperService.fixActiveStageTeam(activeStage, trueActiveStageTeamUuid);

        helperService.migrate(executionHashMap.get("WCS"), 2);
    }

    @Override
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
