package uk.gov.digital.ho.hocs.workflow.util;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.List;
import java.util.function.Consumer;

public class ExecutionVariableSequence implements Consumer<DelegateExecution> {
    private int callCount = 0;
    private final List<List<CallActivityReturnVariable>> variableListList;

    public ExecutionVariableSequence(List<List<CallActivityReturnVariable>> variableListList) {
        this.variableListList = variableListList;
    }

    @Override
    public void accept(DelegateExecution delegateExecution) {

        List<CallActivityReturnVariable> callSequenceVariableList = variableListList.get(callCount++);

        for (CallActivityReturnVariable callActivityReturnVariable : callSequenceVariableList) {
            delegateExecution.setVariable(callActivityReturnVariable.getKey(), callActivityReturnVariable.getValue());
        }
    }
}
