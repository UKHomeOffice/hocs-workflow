package uk.gov.digital.ho.hocs.workflow.util;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CallActivityTestUtil {
    private final String callActivityId;
    private final List<CallActivityVariable> expectedInputVariables = new ArrayList<>();
    private final List<CallActivityVariable> expectedOutputVariables = new ArrayList<>();
    private final String bpmnFile;

    private CallActivityTestUtil(String callActivityId, String bpmnFile) {
        this.callActivityId = callActivityId;
        this.bpmnFile = bpmnFile;
    }

    public static CallActivityTestUtil callActivity(String callActivityId, String bpmnFile) {
        return new CallActivityTestUtil(callActivityId, bpmnFile);
    }

    public CallActivityTestUtil containsInputVariable(String source, String target) {
        expectedInputVariables.add(new CallActivityVariable(source, target));
        return this;
    }

    public CallActivityTestUtil containsOutputVariable(String source, String target) {
        expectedOutputVariables.add(new CallActivityVariable(source, target));
        return this;
    }

    public void assertVariables() {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(getClass().getClassLoader().getResourceAsStream(bpmnFile));

        CallActivity callActivity = modelInstance.getModelElementById(callActivityId);
        ExtensionElements extensionElements = callActivity.getExtensionElements();

        List<CamundaIn> camundaIns = extensionElements.getElementsQuery().filterByType(CamundaIn.class).list();
        List<CallActivityVariable> actualInputVariables = new ArrayList<>();
        for (ModelElementInstance element : camundaIns) {
            actualInputVariables.add(new CallActivityVariable(element.getAttributeValue("source"), element.getAttributeValue("target")));
            actualInputVariables.add(new CallActivityVariable(element.getAttributeValue("sourceExpression"), element.getAttributeValue("target")));
        }

        List<CamundaOut> camundaOuts = extensionElements.getElementsQuery().filterByType(CamundaOut.class).list();
        List<CallActivityVariable> actualOutputVariables = new ArrayList<>();
        for (ModelElementInstance element : camundaOuts) {
            actualOutputVariables.add(new CallActivityVariable(element.getAttributeValue("source"), element.getAttributeValue("target")));
            actualOutputVariables.add(new CallActivityVariable(element.getAttributeValue("sourceExpression"), element.getAttributeValue("target")));
        }

        for (CallActivityVariable expectedInputVariable : expectedInputVariables) {
            String message = "Expected Input : " + expectedInputVariables + " \nActual Input : " + actualInputVariables;
            assertTrue(message, actualInputVariables.contains(expectedInputVariable));
        }

        for (CallActivityVariable expectedOutputVariable : expectedOutputVariables) {
            String message = "Expected Output : " + expectedOutputVariables + " \nActual Output : " + actualOutputVariables;
            assertTrue(message, actualOutputVariables.contains(expectedOutputVariable));
        }
    }
}
