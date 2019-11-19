package uk.gov.digital.ho.hocs.workflow.api;

public enum Direction {

    FORWARD("FORWARD"),
    BACKWARD("BACKWARD");

    private String value;

    Direction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
