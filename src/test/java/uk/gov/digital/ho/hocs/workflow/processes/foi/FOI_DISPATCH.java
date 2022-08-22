package uk.gov.digital.ho.hocs.workflow.processes.foi;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/FOI/FOI_DISPATCH.bpmn"})
public class FOI_DISPATCH {

    public static final String DISPATCH_CONFIRMATION = "DISPATCH_CONFIRMATION";
    public static final String DEALLOCATE_TEAM = "DEALLOCATE_TEAM";
    public static final String FOI_UPLOAD_FINAL_DOCUMENTS = "FOI_UPLOAD_FINAL_DOCUMENTS";
    public static final String SET_DISPATCH_DATE = "SET_DISPATCH_DATE";
    public static final String CLEAR_REJECTED = "CLEAR_REJECTED";
    public static final String SET_TO_REJECTED_BY_DISPATCH = "SET_TO_REJECTED_BY_DISPATCH";
    public static final String END_EVENT = "END_EVENT";
    public static final String CASE_OUTCOME = "CASE_OUTCOME";
    public static final String ADD_EXCEPTIONS = "ADD_EXCEPTIONS";
    public static final String ADD_EXEMPTIONS = "ADD_EXEMPTIONS";
    public static final String CHECK_ANSWERS = "CHECK_ANSWERS";
    public static final String ADD_EXEMPTIONS_AND_EXCEPTIONS = "ADD_EXEMPTIONS_AND_EXCEPTIONS";


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
    private ProcessScenario FOIDataInputProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void doNotDispatch() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-N")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXEMPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(SET_TO_REJECTED_BY_DISPATCH);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);
    }

    @Test
    public void doDispatchFOI() {

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXEMPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchFoiAfterBack() {
        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD","ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXEMPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchFOIWithBack() {

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(ADD_EXEMPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchEIR() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "EIR",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXCEPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXCEPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }


    @Test
    public void doDispatchFOIAndEIR() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI_AND_EIR",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_AND_EIR_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS_AND_EXCEPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXEMPTIONS_AND_EXCEPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchFOIAndEIRAfterExemptionsChange() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI_AND_EIR",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_AND_EIR_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS_AND_EXCEPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "CHANGE_EXEMPTIONS")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXEMPTIONS_AND_EXCEPTIONS);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchFOIAndEIRAfterExceptionsChange() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI_AND_EIR",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_AND_EIR_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS_AND_EXCEPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "CHANGE_EXCEPTIONS")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXCEPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ADD_EXEMPTIONS_AND_EXCEPTIONS);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchFOIAndEIRAfterChangeOutcome() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI_AND_EIR",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_AND_EIR_CASE_TYPE_DISPATCH")))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI_AND_EIR",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_AND_EIR_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS_AND_EXCEPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "CHANGE_OUTCOME")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(ADD_EXEMPTIONS_AND_EXCEPTIONS);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchNoRelease() {

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI_AND_EIR",
                        "TransferOutcome", "REQ_UNCLEAR",
                        "DIRECTION", "DEALLOCATE_TEAM")));

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatchFOIReleasedInFull() {

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI",
                        "TransferOutcome", "RELEASED_IN_FULL",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CASE_OUTCOME);
        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doUploadFinalDocumentsWithBack() {

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        when(FOIDataInputProcess.waitsAtUserTask(CASE_OUTCOME))
                .thenReturn(task -> task.complete(withVariables("CaseType", "FOI",
                        "TransferOutcome", "RELEASED_IN_PART",
                        "DIRECTION", "FOI_CASE_TYPE_DISPATCH")));

        when(FOIDataInputProcess.waitsAtUserTask(ADD_EXEMPTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(FOIDataInputProcess.waitsAtUserTask(FOI_UPLOAD_FINAL_DOCUMENTS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(CASE_OUTCOME);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(ADD_EXEMPTIONS);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(FOI_UPLOAD_FINAL_DOCUMENTS);

        verify(FOIDataInputProcess, times(3))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

}
