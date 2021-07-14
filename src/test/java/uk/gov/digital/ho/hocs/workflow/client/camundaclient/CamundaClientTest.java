package uk.gov.digital.ho.hocs.workflow.client.camundaclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CamundaClientTest {

  private CamundaClient camundaClient;

  @Mock
  private TaskService taskService;

  @Mock
  private TaskQuery taskQuery;

  @Mock
  private Task task;

  @Mock
  private RuntimeService runtimeService;

  @Mock
  private ProcessInstance processInstance;

  @Mock
  private ProcessInstanceQuery processInstanceQuery;

  @Mock
  private VariableInstance variableInstance;

  @Mock
  private VariableInstanceQuery variableInstanceQuery;

  private final UUID stageUUID = UUID.randomUUID();

  private final String FORM_KEY_SCREEN_NAME = "ABC_DEF";

  private final String PROCESS_VARIABLE_SCREEN_NAME = "SCREEN_NAME_2";

  private final String FINISH = "FINISH";

  private final String PROCESS_INSTANCE_ID = "1234";

  private final String SCREEN_PROCESS_VARIABLE = "screen";

  @Before
  public void setUp() {
    camundaClient = new CamundaClient(runtimeService, taskService);
    when(taskService.createTaskQuery()).thenReturn(taskQuery);
    when(taskQuery.processInstanceBusinessKey(any())).thenReturn(taskQuery);
    when(taskQuery.initializeFormKeys()).thenReturn(taskQuery);
    when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
    when(processInstanceQuery.processInstanceBusinessKey(stageUUID.toString())).thenReturn(processInstanceQuery);
    when(processInstanceQuery.singleResult()).thenReturn(processInstance);
    when(processInstance.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    when(runtimeService.createVariableInstanceQuery()).thenReturn(variableInstanceQuery);
    when(variableInstanceQuery.processInstanceIdIn(PROCESS_INSTANCE_ID)).thenReturn(variableInstanceQuery);
    when(variableInstanceQuery.variableName(SCREEN_PROCESS_VARIABLE)).thenReturn(variableInstanceQuery);
  }

  @Test
  public void screenNameTakenFromTaskFormKeyIfPresent() {

    //given
    when(taskQuery.singleResult()).thenReturn(task);
    when(task.getFormKey()).thenReturn(FORM_KEY_SCREEN_NAME);

    //when
    String screenName = camundaClient.getStageScreenName(stageUUID);

    //then
    assertThat(screenName).isEqualTo(FORM_KEY_SCREEN_NAME);
  }

  @Test
  public void screenNameTakenFromProcessVariableIfTaskNotFound() {

    //given
    when(taskQuery.singleResult()).thenReturn(null);
    when(variableInstanceQuery.singleResult()).thenReturn(variableInstance);
    when(variableInstance.getValue()).thenReturn(PROCESS_VARIABLE_SCREEN_NAME);

    //when
    String screenName = camundaClient.getStageScreenName(stageUUID);

    //then
    assertThat(screenName).isEqualTo(PROCESS_VARIABLE_SCREEN_NAME);
  }


  @Test
  public void screenNameTakenFromProcessVariableIfTaskFoundWithoutFormKey() {

    //given
    when(task.getFormKey()).thenReturn(null);
    when(taskQuery.singleResult()).thenReturn(task);
    when(variableInstanceQuery.singleResult()).thenReturn(variableInstance);
    when(variableInstance.getValue()).thenReturn(PROCESS_VARIABLE_SCREEN_NAME);

    //when
    String screenName = camundaClient.getStageScreenName(stageUUID);

    //then
    assertThat(screenName).isEqualTo(PROCESS_VARIABLE_SCREEN_NAME);
  }

  @Test
  public void finishIfFormKeyNotPresentAndProcessVariableNull() {

    //given
    when(taskQuery.singleResult()).thenReturn(null);
    when(variableInstanceQuery.singleResult()).thenReturn(variableInstance);
    when(variableInstance.getValue()).thenReturn(null);

    //when
    String screenName = camundaClient.getStageScreenName(stageUUID);

    //then
    assertThat(screenName).isEqualTo(FINISH);
  }

  @Test
  public void hasProcessInstanceVariableWithValueShouldReturnTrueIfThereAreMatches() {

    //given
    String businessKey = "business-key";
    String variableName = "ABC";
    String value = "123";
    List<ProcessInstance> matches = new ArrayList<>();
    matches.add(mock(ProcessInstance.class));
    when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
    when(processInstanceQuery.processInstanceBusinessKey(businessKey)).thenReturn(processInstanceQuery);
    when(processInstanceQuery.variableValueEquals(variableName, value)).thenReturn(processInstanceQuery);
    when(processInstanceQuery.list()).thenReturn(matches);

    //when
    boolean result = camundaClient.hasProcessInstanceVariableWithValue(businessKey, variableName, value);

    //then
    assertThat(result).isTrue();
  }

  @Test
  public void hasProcessInstanceVariableWithValueShouldReturnFalseIfNoMatches() {

    //given
    String businessKey = "business-key";
    String variableName = "ABC";
    String value = "123";
    List<ProcessInstance> matches = new ArrayList<>();
    when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
    when(processInstanceQuery.processInstanceBusinessKey(businessKey)).thenReturn(processInstanceQuery);
    when(processInstanceQuery.variableValueEquals(variableName, value)).thenReturn(processInstanceQuery);
    when(processInstanceQuery.list()).thenReturn(matches);

    //when
    boolean result = camundaClient.hasProcessInstanceVariableWithValue(businessKey, variableName, value);

    //then
    assertThat(result).isFalse();
  }

  @Test
  public void removeProcessInstanceVariableFromAllScopesInvokesRuntimeServiceCorrectly() {

    //given
    final String variableName = "V_NAME";
    final String caseUUID = "CASE_UUID";
    final String stageUUID = "STAGE_UUID";
    when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
    ProcessInstanceQuery caseProcessInstanceQuery = mock(ProcessInstanceQuery.class);
    ProcessInstanceQuery stageProcessInstanceQuery = mock(ProcessInstanceQuery.class);
    when(processInstanceQuery.processInstanceBusinessKey(caseUUID)).thenReturn(caseProcessInstanceQuery);
    when(processInstanceQuery.processInstanceBusinessKey(stageUUID)).thenReturn(stageProcessInstanceQuery);
    ProcessInstance caseMatch = mock(ProcessInstance.class);
    when(caseMatch.getProcessInstanceId()).thenReturn(caseUUID);
    ProcessInstance stageMatch = mock(ProcessInstance.class);
    when(stageMatch.getProcessInstanceId()).thenReturn(stageUUID);
    List<ProcessInstance> caseMatches = new ArrayList<>();
    caseMatches.add(caseMatch);
    List<ProcessInstance> stageMatches = new ArrayList<>();
    stageMatches.add(stageMatch);
    when(caseProcessInstanceQuery.list()).thenReturn(caseMatches);
    when(stageProcessInstanceQuery.list()).thenReturn(stageMatches);

    //when
    camundaClient.removeProcessInstanceVariableFromAllScopes(caseUUID, stageUUID, variableName);

    //then
    verify(runtimeService).removeVariable(caseUUID, variableName);
    verify(runtimeService).removeVariable(stageUUID, variableName);
  }

}
