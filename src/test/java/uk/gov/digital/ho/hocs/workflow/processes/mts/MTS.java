package uk.gov.digital.ho.hocs.workflow.processes.mts;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.mockito.ProcessExpressions;
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
import uk.gov.digital.ho.hocs.workflow.util.CallActivityReturnVariable;
import uk.gov.digital.ho.hocs.workflow.util.ExecutionVariableSequence;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/MTS.bpmn",
        "processes/MTS_DATA_INPUT.bpmn",
        "processes/STAGE.bpmn"
})
public class MTS {

    public static final String DATA_INPUT_CALL_ACTIVITY = "CallActivity_14mwb9u";

    public static final String COMPLETE_CASE_SERVICE_TASK = "ServiceTask_1umfuu0";

    public static final String END_EVENT = "EndEvent_0h34pj4";

    private static final Map<String, Object> PROCESS_INSTANCE_VARS = new HashMap<>();


    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario mtsProcess;

    @Before
    public void defaultScenario() {
        PROCESS_INSTANCE_VARS.put("StageUUID", "RANDOM_UUID_AS_STRING");
        Mocks.register("bpmnService", bpmnService);

        ProcessExpressions.registerCallActivityMock("MTS_DATA_INPUT")
                .deploy(rule);
    }

    @Test
    public void happyPath() {
        Scenario.run(mtsProcess)
                .startByKey("MTS", PROCESS_INSTANCE_VARS)
                .execute();

        verify(mtsProcess)
                .hasCompleted(DATA_INPUT_CALL_ACTIVITY);

        verify(mtsProcess)
                .hasCompleted(COMPLETE_CASE_SERVICE_TASK);

        verify(mtsProcess).hasFinished(END_EVENT);
    }
}
