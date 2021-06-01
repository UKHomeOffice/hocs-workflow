package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP_REGISTRATION.bpmn")
public class COMP_REGISTRATION {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.91)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario compRegistrationProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);

        when(compRegistrationProcess.waitsAtUserTask("Validate_CorrespondentInput"))
                .thenReturn(task -> task.complete(withVariables("valid", true)));

        when(bpmnService.caseHasPrimaryCorrespondentType(any(), eq("COMPLAINANT"))).thenReturn(true);

        when(compRegistrationProcess.waitsAtUserTask("Validate_Complainant"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(compRegistrationProcess.waitsAtUserTask("Validate_Complaint"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(compRegistrationProcess.waitsAtUserTask("UserTask_0k00jya"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(compRegistrationProcess.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Not_Service")));
    }

    @Test
    public void happyPath() {
        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess)
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateComplainant(){
        when(compRegistrationProcess.waitsAtUserTask("Validate_Complainant"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess, times(2))
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void hasNoPrimaryCorrespondent(){
        when(bpmnService.caseHasPrimaryCorrespondentType(any(), eq("COMPLAINANT")))
                .thenReturn(false)
                .thenReturn(true);

        when(compRegistrationProcess.waitsAtUserTask("Validate_InvalidCorrespondents"))
                .thenReturn(task -> task.complete(withVariables("valid", true)));

        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess, times(2))
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("Screen_InvalidCorrespondents");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateComplaint(){
        when(compRegistrationProcess.waitsAtUserTask("Validate_Complaint"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess)
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateInput(){
        when(compRegistrationProcess.waitsAtUserTask("UserTask_0k00jya"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess)
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateCategory(){
        when(compRegistrationProcess.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "CompType", "Not_Service")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Not_Service")));

        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess)
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void updateTeamForServiceTriage(){
        when(compRegistrationProcess.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Service")));

        Scenario.run(compRegistrationProcess)
                .startByKey("COMP_REGISTRATION")
                .execute();

        verify(compRegistrationProcess)
                .hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess)
                .hasCompleted("ServiceTask_0pumxnf");

        verify(compRegistrationProcess)
                .hasCompleted("Service_UpdateTeamByStageAndTexts");
    }
}
