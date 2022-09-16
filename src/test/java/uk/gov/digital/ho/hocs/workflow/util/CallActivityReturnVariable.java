package uk.gov.digital.ho.hocs.workflow.util;

public class CallActivityReturnVariable {

    String key;

    String value;

    public CallActivityReturnVariable(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}