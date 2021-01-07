package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.delegate.DelegateExecution;
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

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/DCU_MIN.bpmn",
        "processes/DCU_BASE_DATA_INPUT.bpmn",
        "processes/DCU_MIN_MARKUP.bpmn",
        "processes/DCU_MIN_INITIAL_DRAFT.bpmn",
        "processes/DCU_BASE_QA_RESPONSE.bpmn",
        "processes/DCU_MIN_MINISTER_SIGN_OFF.bpmn",
        "processes/DCU_MIN_PRIVATE_OFFICE.bpmn",
        "processes/DCU_BASE_DISPATCH.bpmn",
        "processes/STAGE.bpmn",
        "processes/STAGE_WITH_USER.bpmn"})
public class DCU_MIN {

    public static final String APPROVE_MINISTER_SIGN_OFF = "UserTask_0eagz4p";
    public static final String MIN_SIGN_OFF = "CallActivity_1syv7pu";
    public static final String DISPATCH = "CallActivity_1rowgu5";
    public static final String PO_SIGN_OFF = "CallActivity_0pmeblj";
    public static final String INITIAL_DRAFT = "CallActivity_1ket68y";
    public static final String APPROVE_MIN_SIGN_OFF = "ServiceTask_0te5zh0";
    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario dcuMinProcess;
    @Mock
    private ProcessScenario minSignOffProcess;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        ProcessExpressions.registerCallActivityMock("DCU_BASE_DATA_INPUT")
                .onExecutionAddVariable("CopyNumberTen", "FALSE")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("DCU_MIN_MARKUP")
                .onExecutionAddVariable("MarkupDecision", "PR")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("DCU_MIN_INITIAL_DRAFT")
                .onExecutionAddVariable("InitialDraftDecision", "ACCEPT")
                .onExecutionAddVariable("OfflineQA", "FALSE")
                .onExecutionAddVariable("ResponseChannel", "EMAIL")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("DCU_BASE_QA_RESPONSE")
                .onExecutionAddVariable("QAResponseDecision", "ACCEPT")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("DCU_MIN_PRIVATE_OFFICE")
                .onExecutionAddVariable("PrivateOfficeDecision", "ACCEPT")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("DCU_BASE_DISPATCH")
                .onExecutionAddVariable("DispatchDecision", "ACCEPT")
                .deploy(rule);
    }

    @Test
    public void acceptMinisterSignOffScenario() {

        ProcessExpressions.registerCallActivityMock("DCU_MIN_MINISTER_SIGN_OFF")
                .onExecutionAddVariable("MinisterSignOffDecision", "ACCEPT")
                .deploy(rule);

        Scenario.run(dcuMinProcess)
                .startByKey("MIN")
                .execute();

        verify(dcuMinProcess, times(1))
                .hasCompleted(INITIAL_DRAFT);

        verify(dcuMinProcess, times(1))
                .hasCompleted(DISPATCH);

    }

    @Test
    public void rejectMinisterSignOffScenario() {

        // A DelegateExecution consumer has had to be introduced to allow different values to be set for different calls.
        // In this case we need the first call to be Reject and the second accept
        final Consumer<DelegateExecution> rejectThenAcceptConsumer = new Consumer<>() {
            private int callCount = 1;
            @Override
            public void accept(DelegateExecution delegateExecution) {
                if (callCount == 1) {
                    delegateExecution.setVariable("MinisterSignOffDecision", "REJECT");
                    ++callCount;
                } else {
                    delegateExecution.setVariable("MinisterSignOffDecision", "ACCEPT");
                }

            }
        };

        ProcessExpressions.registerCallActivityMock("DCU_MIN_MINISTER_SIGN_OFF")
                .onExecutionDo(rejectThenAcceptConsumer)
                .deploy(rule);

        Scenario.run(dcuMinProcess)
                .startByKey("MIN")
                .execute();

        verify(dcuMinProcess, times(2))
                .hasCompleted(INITIAL_DRAFT);

        verify(dcuMinProcess, times(1))
                .hasCompleted(DISPATCH);
    }
}
