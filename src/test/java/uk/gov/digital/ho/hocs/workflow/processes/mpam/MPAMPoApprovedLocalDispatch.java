package uk.gov.digital.ho.hocs.workflow.processes.mpam;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.List;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_PO_APPROVED_LOCAL_DISPATCH.bpmn")
public class MPAMPoApprovedLocalDispatch {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(1)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void isValidInput_ThenEndStage() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "MPAMDispatchStatus", "DispatchAndClose",
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "MPAMDispatchStatus", "DispatchAndClose",
                        "valid", true
                )));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO_APPROVED_LOCAL_DISPATCH")
                .execute();

        verify(processScenario).hasFinished("EndEvent_MPAMPoApprovedLocalDispatch");
    }

    @Test
    public void setDirectionBackward_ThenEndStage() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "MPAMDispatchStatus", "MoveBack",
                        "valid", true
                )));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO_APPROVED_LOCAL_DISPATCH")
                .execute();

        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By Dispatch"));
        verify(processScenario).hasCompleted("Activity_0ry80sz");
        verify(processScenario).hasFinished("EndEvent_MPAMPoApprovedLocalDispatch");
    }

    @After
    public void after(){
        RepositoryService repositoryService = rule.getRepositoryService();

        List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }

        Mocks.reset();
    }
}
