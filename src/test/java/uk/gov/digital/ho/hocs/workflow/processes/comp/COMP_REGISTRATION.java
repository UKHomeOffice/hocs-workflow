package uk.gov.digital.ho.hocs.workflow.processes.comp;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
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

import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_REGISTRATION.bpmn")
public class COMP_REGISTRATION {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario compRegistrationProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);

        when(compRegistrationProcess.waitsAtUserTask("Screen_Correspondents")).thenReturn(
            task -> task.complete(withVariables("valid", true)));

        when(bpmnService.caseHasPrimaryCorrespondentType(any(), eq("COMPLAINANT"))).thenReturn(true);

        when(compRegistrationProcess.waitsAtUserTask("Screen_Complainant")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(compRegistrationProcess.waitsAtUserTask("Screen_Complaint")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Service")));

        when(compRegistrationProcess.waitsAtUserTask("Screen_RegistrationInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(compRegistrationProcess.waitsAtUserTask("Screen_Category")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Not_Service")));
    }

    @Test
    public void happyPath() {
        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void webformHappyPath() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_WebformComplaint")).thenReturn(TaskDelegate::complete);

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION",
            Map.of("XOriginatedFrom", "Webform")).execute();

        verify(compRegistrationProcess).hasCompleted("Screen_WebformComplaint");

        verify(compRegistrationProcess).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void webformNotValidPath() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_WebformComplaint")).thenReturn(
            task -> task.complete(withVariables("WebformComplaintInvalid", "Yes")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION",
            Map.of("XOriginatedFrom", "Webform")).execute();

        verify(compRegistrationProcess).hasCompleted("Screen_WebformComplaint");

        verify(compRegistrationProcess, times(0)).hasCompleted("Screen_Correspondents");

        verify(compRegistrationProcess).hasCompleted("EndEvent_COMP_WEBFORM_CREATION");
    }

    @Test
    public void backwardsFromValidateComplainant() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Complainant")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess, times(2)).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void hasNoPrimaryCorrespondent() {
        when(bpmnService.caseHasPrimaryCorrespondentType(any(), eq("COMPLAINANT"))).thenReturn(false).thenReturn(true);

        when(compRegistrationProcess.waitsAtUserTask("Screen_InvalidCorrespondents")).thenReturn(TaskDelegate::complete);

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess, times(2)).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("Screen_InvalidCorrespondents");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateComplaint() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Complaint")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Service")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateInput() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_RegistrationInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void backwardsFromValidateCategory() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Category")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD", "CompType", "Not_Service"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Not_Service")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");
    }

    @Test
    public void updateTeamForServiceTriage() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Category")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Service")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("Service_CaseHasPrimaryCorrespondentType");

        verify(compRegistrationProcess).hasCompleted("ServiceTask_0pumxnf");

        verify(compRegistrationProcess).hasCompleted("Service_UpdateTeamByStageAndTexts");
    }

    @Test
    public void skipCategoryIfExGratia() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Complaint")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Ex-Gratia")));

        when(compRegistrationProcess.waitsAtUserTask("ExGratia_Input")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD", "CompType", "Service"))).thenReturn(
            task -> task.complete(
                withVariables("DIRECTION", "FORWARD", "CompType", "Ex-Gratia")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("UpdateTeamForExGratia");
    }

    @Test
    public void assignToMinorMisconduct() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Complaint")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "MinorMisconduct")));

        when(compRegistrationProcess.waitsAtUserTask("ExGratia_Input")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD", "CompType", "Service"))).thenReturn(
            task -> task.complete(
                withVariables("DIRECTION", "FORWARD", "CompType", "MinorMisconduct"))).thenReturn(
            task -> task.complete(
                withVariables("DIRECTION", "FORWARD", "CompType", "MinorMisconduct")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess).hasCompleted("UpdateTeamForMinorMisconduct");
    }

    @Test
    public void compTypeSeriousMisconduct() {
        when(compRegistrationProcess.waitsAtUserTask("Screen_Complaint")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "SeriousMisconduct")));

        when(compRegistrationProcess.waitsAtUserTask("Activity_ScreenCategorySerious")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(compRegistrationProcess.waitsAtUserTask("Screen_ComplaintDetailsSerious")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(compRegistrationProcess).startByKey("COMP_REGISTRATION").execute();

        verify(compRegistrationProcess, times(4)).hasCompleted("Activity_ScreenCategorySerious");
        verify(compRegistrationProcess, times(3)).hasCompleted("Screen_ComplaintDetailsSerious");
    }
}
