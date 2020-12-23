package uk.gov.digital.ho.hocs.workflow.processes;

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

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/MPAM.bpmn",
        "processes/MPAM_CREATION.bpmn",
        "processes/MPAM_CAMPAIGN.bpmn",
        "processes/MPAM_CREATION.bpmn",
        "processes/MPAM_DISPATCH.bpmn",
        "processes/MPAM_DISPATCHED_FOLLOW_UP.bpmn",
        "processes/MPAM_DRAFT.bpmn",
        "processes/MPAM_DRAFT_ESCALATE.bpmn",
        "processes/MPAM_DRAFT_ONHOLD.bpmn",
        "processes/MPAM_DRAFT_REQUESTED_CONTRIBUTION.bpmn",
        "processes/MPAM_PO.bpmn",
        "processes/MPAM_QA.bpmn",
        "processes/MPAM_QA_ESCALATE.bpmn",
        "processes/MPAM_QA_ONHOLD.bpmn",
        "processes/MPAM_TRIAGE.bpmn",
        "processes/MPAM_TRIAGE_ESCALATE.bpmn",
        "processes/MPAM_TRIAGE_ON_HOLD.bpmn",
        "processes/MPAM_TRIAGE_REQUESTED_CONTRIBUTION.bpmn",
        "processes/STAGE.bpmn",
        "processes/STAGE_WITH_USER.bpmn"})
public class MPAM {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario mpamProcess;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        ProcessExpressions.registerCallActivityMock("MPAM_CREATION")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("RefType", "Ministerial")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_QA")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_PO")
                .onExecutionAddVariable("PoStatus", "Dispatched-Follow-Up")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DISPATCHED_FOLLOW_UP")
                .deploy(rule);
    }

    @Test
    public void happyPath() {

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_1c4zy60");
        verify(mpamProcess)
                .hasCompleted("ServiceTask_0rwk9ie");

    }

}
