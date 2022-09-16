package uk.gov.digital.ho.hocs.workflow.processes.to;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/TO/TO_CHANGE_BUSINESS_AREA.bpmn" })
public class TO_CHANGE_BUSINESS_AREA {

    // COMMON GATEWAY OUTCOMES
    private static final String BACKWARD = "BACKWARD";

    private static final String FORWARD = "FORWARD";

    private static final String UKVI = "UKVI";

    private static final String BF = "BF";

    private static final String HMPO = "HMPO";

    // USER AND SERVICE TASKS
    private static final String TO_SELECT_BUSINESS_AREA = "TO_SELECT_BUSINESS_AREA";

    private static final String ALLOCATE_TO_UKVI_TEAM = "Activity_0cvzfw5";

    private static final String ALLOCATE_TO_BF_TEAM = "Activity_1iidtq7";

    private static final String ALLOCATE_TO_HMPO_TEAM = "Activity_18m7196";

    private static final String UPDATE_BUS_AREA_STATUS = "Activity_1t646w4";

    private static final String CLEAR_REALLOCATION_NOTE = "Activity_0sgqe01";

    private static final String SAVE_REALLOCATION_NOTE = "Activity_0vi156d";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario TOProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void shouldRegisterBusinessAreaUKVI() {

        when(TOProcess.waitsAtUserTask(TO_SELECT_BUSINESS_AREA)).thenReturn(
            task -> task.complete(withVariables("BusinessArea", UKVI, "DIRECTION", FORWARD)));

        Scenario.run(TOProcess).startByKey("TO_CHANGE_BUSINESS_AREA").execute();

        verify(TOProcess, times(1)).hasCompleted(CLEAR_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(SAVE_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(TO_SELECT_BUSINESS_AREA);

        verify(TOProcess, times(1)).hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1)).hasCompleted(UPDATE_BUS_AREA_STATUS);

    }

    @Test
    public void shouldRegisterBusinessAreaBF() {

        when(TOProcess.waitsAtUserTask(TO_SELECT_BUSINESS_AREA)).thenReturn(
            task -> task.complete(withVariables("BusinessArea", BF, "DIRECTION", FORWARD)));

        Scenario.run(TOProcess).startByKey("TO_CHANGE_BUSINESS_AREA").execute();

        verify(TOProcess, times(1)).hasCompleted(CLEAR_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(SAVE_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(TO_SELECT_BUSINESS_AREA);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(1)).hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1)).hasCompleted(UPDATE_BUS_AREA_STATUS);

    }

    @Test
    public void shouldRegisterBusinessAreaHMPO() {

        when(TOProcess.waitsAtUserTask(TO_SELECT_BUSINESS_AREA)).thenReturn(
            task -> task.complete(withVariables("BusinessArea", HMPO, "DIRECTION", FORWARD)));

        Scenario.run(TOProcess).startByKey("TO_CHANGE_BUSINESS_AREA").execute();

        verify(TOProcess, times(1)).hasCompleted(CLEAR_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(SAVE_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(TO_SELECT_BUSINESS_AREA);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(1)).hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1)).hasCompleted(UPDATE_BUS_AREA_STATUS);

    }

    @Test
    public void shouldNotRegisterBusinessArea() {

        when(TOProcess.waitsAtUserTask(TO_SELECT_BUSINESS_AREA)).thenReturn(
            task -> task.complete(withVariables("DIRECTION", BACKWARD)));

        Scenario.run(TOProcess).startByKey("TO_CHANGE_BUSINESS_AREA").execute();

        verify(TOProcess, times(1)).hasCompleted(CLEAR_REALLOCATION_NOTE);

        verify(TOProcess, times(0)).hasCompleted(SAVE_REALLOCATION_NOTE);

        verify(TOProcess, times(1)).hasCompleted(TO_SELECT_BUSINESS_AREA);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0)).hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(0)).hasCompleted(UPDATE_BUS_AREA_STATUS);

    }

}
