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
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/SMC_TRIAGE.bpmn")
public class SMC_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario process;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath(){
        when(process.waitsAtUserTask("Validate_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_PSU"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Reasons"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "ScmTriageResult", "Continue")));

        Scenario.run(process).startByKey("SMC_TRIAGE").execute();

        verify(process).hasCompleted("EndEvent_SCM_TRIAGE");
    }

    @Test
    public void testCloseAtTriage(){
        when(process.waitsAtUserTask("Validate_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_PSU"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Reasons"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "ScmTriageResult", "Complete")));

        when(process.waitsAtUserTask("Validate_Complete_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Complete_Confirm"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "No")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(process).startByKey("SMC_TRIAGE").execute();

        verify(bpmnService).updateAllocationNote(any(), any(), any(), eq("CLOSE"));
        verify(process).hasCompleted("EndEvent_SCM_TRIAGE");
    }

    @Test
    public void testReferAtTriage(){
        when(process.waitsAtUserTask("Validate_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_PSU"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Reasons"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "ScmTriageResult", "NoResponse")));

        when(process.waitsAtUserTask("Validate_Refer_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Refer"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "No")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(process).startByKey("SMC_TRIAGE").execute();

        verify(bpmnService).updateAllocationNote(any(), any(), any(), eq("REFER"));
        verify(process).hasCompleted("EndEvent_SCM_TRIAGE");
    }

    @Test
    public void testSaveAtTriage() {
        when(process.waitsAtUserTask("Validate_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_PSU"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Reasons"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "ScmTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "ScmTriageResult", "NotPending")));

        Scenario.run(process).startByKey("SMC_TRIAGE").execute();

        verify(process).hasCompleted("EndEvent_SCM_TRIAGE");
    }

    @Test
    public void testTransferCaseToCCH(){
        when(process.waitsAtUserTask("Validate_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "No")));

        when(process.waitsAtUserTask("Transfer_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "TransferType", "CCH")));

        Scenario.run(process).startByKey("SMC_TRIAGE").execute();

        verify(process).hasCompleted("Activity_0wzbpdx");
    }

    @Test
    public void testTransferCaseToIEDET(){
        when(process.waitsAtUserTask("Validate_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "No")));

        when(process.waitsAtUserTask("Transfer_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "TransferType", "IEDET")));

        Scenario.run(process).startByKey("SMC_TRIAGE").execute();

        verify(process).hasCompleted("Activity_0vcth0f");
    }

}
