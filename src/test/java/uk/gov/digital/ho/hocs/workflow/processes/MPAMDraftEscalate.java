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
@Deployment(resources = "processes/MPAM_DRAFT_ESCALATE.bpmn")
public class MPAMDraftEscalate {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.1)
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
    public void whenMinisterialChangedToOfficial_thenMinisterialValuesAreCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Ministerial",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", true,
                        "CaseNote_TriageChangeCaseType", "Casenote")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT_ESCALATE")
                .execute();

        verify(processScenario, times(3)).hasCompleted("Screen_ReferenceTypeToOfficial");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToOfficial");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(processScenario).hasCompleted("Service_ClearMinisterialValues");
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote");
        verify(bpmnService).createCaseConversionNote(any(), any(), eq("Casenote"));
        verify(processScenario).hasFinished("EndEvent_MpamDraftEscalate");
    }

    @Test
    public void whenOfficialChangedToMinisterial_thenMinisterialValuesAreNotCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Official",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", true,
                        "CaseNote_TriageChangeCaseType", "Casenote")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT_ESCALATE")
                .execute();

        verify(processScenario, times(3)).hasCompleted("Screen_ReferenceTypeToMinisterial");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToMinisterial");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote");
        verify(bpmnService).createCaseConversionNote(any(), any(), eq("Casenote"));
        verify(processScenario).hasFinished("EndEvent_MpamDraftEscalate");
        verify(bpmnService, never()).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }
}
