package uk.gov.digital.ho.hocs.workflow.processes.comp2;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMP2/COMP2.bpmn",
    "processes/COMP2/COMP2_CCH.bpmn", "processes/COMP2/COMP2_CLOSED.bpmn", "processes/COMP2/COMP2_MINOR_CHECK.bpmn",
    "processes/COMP2/COMP2_MINOR_RESP.bpmn", "processes/COMP2/COMP2_OTHER.bpmn", "processes/COMP2/COMP2_REGISTRATION.bpmn",
    "processes/COMP2/COMP2_RECATEGORISE.bpmn", "processes/COMP2/COMP2_SERVICE.bpmn", "processes/COMP2/COMP2_EXGRATIA.bpmn",
    "processes/COMP2/COMP2_MINORMISCONDUCT.bpmn", "processes/STAGE.bpmn", "processes/STAGE_WITH_USER.bpmn",
    "processes/PSU/PSU_COMP2_COMPLAINT.bpmn"
})

public class COMP2 {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.90).build();

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
    public void testWhenService_CompleteCase() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage2").deploy(rule);
        whenAtCallActivity("COMP2_SERVICE").thenReturn("CompType", "Service").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_SERVICE");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testWhenService_ReturnToCch() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage2").deploy(rule);
        whenAtCallActivity("COMP2_SERVICE").thenReturn("CompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP2_CCH").thenReturn("CompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_CCH");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeService_EscalateToPsu() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP2_SERVICE").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeServiceEscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP2_SERVICE").thenReturn("CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP2_SERVICE").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromQa() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP2_SERVICE").thenReturn("CctQaResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testWhenMinor() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Minor", "Stage", "Stage2").deploy(rule);
        whenAtCallActivity("COMP2_MINOR_CHECK").thenReturn("MinorAccept", "Yes").deploy(rule);
        whenAtCallActivity("COMP2_MINOR_RESP").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINOR_CHECK");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINOR_RESP");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testWhenExGratia() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage2").deploy(rule);
        whenAtCallActivity("COMP2_EXGRATIA").thenReturn("CompType", "Ex-Gratia").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testWhenExGratia_ReturnToCch() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage2").deploy(rule);
        whenAtCallActivity("COMP2_EXGRATIA").thenReturn("CctTriageAccept", "No", "CompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP2_CCH").thenReturn("CompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_CCH");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsu() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP2_EXGRATIA").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP2_EXGRATIA").thenReturn("CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP2_EXGRATIA").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromQa() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP2_EXGRATIA").thenReturn("CctQaResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testWhenMinorMisconduct() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage2").deploy(
            rule);
        whenAtCallActivity("COMP2_MINORMISCONDUCT").thenReturn("CompType", "MinorMisconduct").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testWhenMinorMisconduct_ReturnToCch() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage2").deploy(
            rule);
        whenAtCallActivity("COMP2_MINORMISCONDUCT").thenReturn("CompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP2_CCH").thenReturn("CompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_CCH");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsu() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP2_MINORMISCONDUCT").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP2_MINORMISCONDUCT").thenReturn("CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP2_MINORMISCONDUCT").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void whenPsuCompletesCase() {
        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "SeriousMisconduct").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario).hasCompleted("StartEvent_COMP2");
        verify(processScenario).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_COMP2");
    }

    @Test
    public void whenPsuRejectsCaseOnce() {
        whenAtCallActivity("COMP2_REGISTRATION").thenReturn("CompType", "SeriousMisconduct").deploy(rule);
        whenAtCallActivity("PSU_COMP2_COMPLAINT")
            .thenReturn("ReturnCase", "true")
            .thenReturn("ReturnCase", "false")
            .deploy(rule);

        whenAtCallActivity("COMP2_RECATEGORISE").thenReturn("CompType", "SeriousMisconduct").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP2").execute();

        verify(processScenario).hasCompleted("StartEvent_COMP2");
        verify(processScenario).hasCompleted("CallActivity_COMP2_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP2_PSU");
        verify(processScenario).hasCompleted("CallActivity_COMP2_RECATEGORISE");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_COMP2");
    }

    @After
    public void after() {
        RepositoryService repositoryService = processEngineRule.getRepositoryService();

        List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }

        Mocks.reset();
    }

}
