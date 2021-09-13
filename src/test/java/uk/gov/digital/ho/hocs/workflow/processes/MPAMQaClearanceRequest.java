package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.test.Deployment;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM_QA_CLEARANCE_REQ.bpmn")
public class MPAMQaClearanceRequest {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(1)
            .build();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario process;

    @Before
    public void before() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void clearanceCancelled(){
        when(process.waitsAtUserTask("Validate_Clearance_Fulfilment"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClearanceStatus", "Cancelled")));

        Scenario.run(process)
                .startByKey("MPAM_QA_CLEARANCE_REQ")
                .execute();

        verify(process).hasCompleted("mpam_clearance_end");
    }

    @Test
    public void clearanceRejected() {
        when(process.waitsAtUserTask("Validate_Clearance_Fulfilment"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClearanceStatus", "RejectDraft")));

        Scenario.run(process)
                .startByKey("MPAM_QA_CLEARANCE_REQ")
                .execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), any(), any(), any(), any());
    }

    @Test
    public void clearanceAccepted() {
        when(process.waitsAtUserTask("Validate_Clearance_Fulfilment"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClearanceStatus", "ApprovePO")));

        Scenario.run(process)
                .startByKey("MPAM_QA_CLEARANCE_REQ")
                .execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO"), any(), any(), any(), any());
    }

}
