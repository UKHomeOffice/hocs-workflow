package uk.gov.digital.ho.hocs.workflow.util;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.camunda.community.mockito.ProcessExpressions.registerCallActivityMock;

/**
 * A wrapper class to simplify workflow testing
 */
public class CallActivityMockWrapper {
    private final String callActivityName;
    private List<List<CallActivityReturnVariable>> variableListList;
    private final VariableMap variableMap = new VariableMapImpl();
    private boolean thenReturnCalled;
    private boolean alwaysReturnCalled;

    public static CallActivityMockWrapper whenAtCallActivity(String callActivityName) {
        return new CallActivityMockWrapper(callActivityName);
    }

    public CallActivityMockWrapper(String callActivityName) {
        this.callActivityName = callActivityName;
    }

    /**
     * Every mock call of the callActivity will return the same values
     */
    public CallActivityMockWrapper alwaysReturn(final String key, final Object value, final Object... furtherKeyValuePairs) {
        if (thenReturnCalled) {
            throw new IllegalStateException("Can't call thenReturn and alwaysReturn together");
        }
        alwaysReturnCalled = true;

        Map<String, Object> stringObjectMap = withVariables(key, value, furtherKeyValuePairs);

        for (Map.Entry<String, Object> inVars : stringObjectMap.entrySet()) {
            variableMap.put(inVars.getKey(), inVars.getValue());
        }
        return this;
    }

    /**
     * This method can be chained together to allow a different set of variable/values to be returned for each call to this callActivity mock
     */
    public CallActivityMockWrapper thenReturn(final String key, final Object value, final Object... furtherKeyValuePairs) {
        if (alwaysReturnCalled) {
            throw new IllegalStateException("Can't call alwaysReturn and thenReturn together");
        }
        thenReturnCalled = true;

        if (variableListList == null) {
            variableListList = new ArrayList<>();
        }
        Map<String, Object> stringObjectMap = withVariables(key, value, furtherKeyValuePairs);
        List<CallActivityReturnVariable> callVariables = new ArrayList<>();
        for (Map.Entry<String, Object> inVars : stringObjectMap.entrySet()) {
            callVariables.add(new CallActivityReturnVariable(inVars.getKey(), (String) inVars.getValue()));
        }
        variableListList.add(callVariables);
        return this;
    }

    public void deploy(final ProcessEngineServices processEngineServices) {
        if (thenReturnCalled) {
            registerCallActivityMock(callActivityName)
                    .onExecutionDo(new ExecutionVariableSequence(variableListList))
                    .deploy(processEngineServices);
        } else if (alwaysReturnCalled) {
            registerCallActivityMock(callActivityName)
                    .onExecutionAddVariables(variableMap)
                    .deploy(processEngineServices);
        } else {
            registerCallActivityMock(callActivityName)
                    .deploy(processEngineServices);
        }
    }
}
