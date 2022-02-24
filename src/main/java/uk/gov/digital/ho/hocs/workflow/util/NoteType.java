package uk.gov.digital.ho.hocs.workflow.util;

public enum NoteType {
    CASE_DATA("Case data updated"),
    EX_GRATIA_UPDATE("Ex-Gratia data updated"),
    ALLOCATE("Allocated to new user or team.");

    private final String defaultMessage;

    NoteType(String s) {
        this.defaultMessage = s;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
