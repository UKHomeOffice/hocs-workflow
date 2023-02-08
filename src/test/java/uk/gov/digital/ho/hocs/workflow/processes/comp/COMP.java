package uk.gov.digital.ho.hocs.workflow.processes.comp;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMP/COMP.bpmn",
    "processes/COMP/COMP_CCH.bpmn", "processes/COMP/COMP_CLOSED.bpmn", "processes/COMP/COMP_MINOR_CHECK.bpmn",
    "processes/COMP/COMP_MINOR_RESP.bpmn", "processes/COMP/COMP_OTHER.bpmn", "processes/COMP/COMP_REGISTRATION.bpmn",
    "processes/COMP/COMP_RECATEGORISE.bpmn", "processes/COMP/COMP_SERVICE.bpmn", "processes/COMP/COMP_EXGRATIA.bpmn",
    "processes/COMP/COMP_MINORMISCONDUCT.bpmn", "processes/STAGE.bpmn", "processes/STAGE_WITH_USER.bpmn",
    "processes/PSU/PSU_COMP_COMPLAINT.bpmn"
})

public class COMP {

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

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE").thenReturn("CompType", "Service").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenService_ReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE").thenReturn("CompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP_CCH").thenReturn("CompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_CCH");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeService_EscalateToPsu() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeServiceEscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE").thenReturn("CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromQa() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE").thenReturn("CctQaResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinor() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Minor", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_MINOR_CHECK").thenReturn("MinorAccept", "Yes").deploy(rule);
        whenAtCallActivity("COMP_MINOR_RESP").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINOR_CHECK");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINOR_RESP");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenExGratia() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA").thenReturn("CompType", "Ex-Gratia").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenExGratia_ReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA").thenReturn("CctTriageAccept", "No", "CompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP_CCH").thenReturn("CompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_CCH");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsu() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA").thenReturn("CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromQa() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA").thenReturn("CctQaResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinorMisconduct() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage1").deploy(
            rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT").thenReturn("CompType", "MinorMisconduct").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinorMisconduct_ReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage1").deploy(
            rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT").thenReturn("CompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP_CCH").thenReturn("CompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_CCH");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsu() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT").thenReturn("CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void whenWebformClosureCompletesCase() {
        whenAtCallActivity("COMP_REGISTRATION").thenReturn("WebformComplaintInvalid", "Yes").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP", Map.of("Channel", "Webform")).execute();

        verify(processScenario).hasCompleted("StartEvent_COMP");
        verify(processScenario).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void whenPsuCompletesCase() {
        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "SeriousMisconduct").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario).hasCompleted("StartEvent_COMP");
        verify(processScenario).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void whenPsuRejectsCaseOnce() {
        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "SeriousMisconduct").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT")
            .thenReturn("ReturnCase", "true")
            .thenReturn("ReturnCase", "false")
            .deploy(rule);

        whenAtCallActivity("COMP_RECATEGORISE").thenReturn("CompType", "SeriousMisconduct").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario).hasCompleted("StartEvent_COMP");
        verify(processScenario).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario).hasCompleted("CallActivity_COMP_RECATEGORISE");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_COMP");
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
