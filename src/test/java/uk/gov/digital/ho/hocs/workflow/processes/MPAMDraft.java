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
@Deployment(resources = "processes/MPAM_DRAFT.bpmn")
public class MPAMDraft {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.2)
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
    public void whenOfficialChangedToMinisterial_thenMinisterialValuesAreNotCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput_00s8k9f"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Official",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial_0zaqr6s"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Screen_ReferenceTypeToMinisterial_1unkg32");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToMinisterial_0urk37h");
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote_1yc1ce2");
        verify(processScenario).hasFinished("EndEvent_168jcnt");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService, never()).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }

    @Test
    public void whenMinisterialChangedToOfficial_thenMinisterialValuesAreCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput_00s8k9f"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Ministerial",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_RefTypeToOfficial_08l10xy"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Screen_RefTypeToOfficial_0ia1i5c");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToOfficial_0lic0m6");
        verify(processScenario).hasCompleted("Service_ClearMinisterialValues_0xqy6p5");
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote_1yc1ce2");
        verify(processScenario).hasFinished("EndEvent_168jcnt");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }
}
