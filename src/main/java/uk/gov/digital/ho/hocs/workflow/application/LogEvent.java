package uk.gov.digital.ho.hocs.workflow.application;

public enum LogEvent {
    REST_HELPER_NOT_FOUND,
    REST_HELPER_INTERNAL_SERVER_ERROR,
    REST_HELPER_MALFORMED_RESPONSE;
    public static final String EVENT = "event_id";
}
