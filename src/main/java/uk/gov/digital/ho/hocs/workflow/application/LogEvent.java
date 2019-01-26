package uk.gov.digital.ho.hocs.workflow.application;

public enum LogEvent {
    TASK_COMPLETED,
    CURRENT_STAGE_RETRIEVED,
    TASK_RETRIEVAL_FAILURE,
    CASE_STARTED_SUCCESS,
    CASE_STARTED_FAILURE,
    CASE_UPDATE_FAILURE,
    STAGE_CREATION_FAILURE,
    CASE_NOTE_CREATION_FAILURE,
    STAGE_USER_NOT_FOUND,
    STAGE_TEAM_UPDATE_FAILURE,
    CASE_TYPE_NOT_FOUND,
    STAGE_NOT_FOUND,
    CASE_NOT_FOUND,
    CREATE_CASE_FAILURE,
    CREATE_CASE_SUCCESS,
    SECURITY_PARSE_ERROR,
    SECURITY_UNAUTHORISED,
    SECURITY_CASE_NOT_ALLOCATED_TO_USER,
    SECURITY_CASE_NOT_ALLOCATED_TO_TEAM,
    INVALID_ACCESS_LEVEL_FOUND,
    DOCUMENT_CLIENT_CREATE_SUCCESS,
    DOCUMENT_CLIENT_PROCESS_SUCCESS,
    DOCUMENT_CLIENT_FAILURE,
    DOCUMENT_CLIENT_DELETE_SUCCESS,
    UNCAUGHT_EXCEPTION,
    REST_HELPER_NOT_FOUND,
    REST_HELPER_INTERNAL_SERVER_ERROR,
    REST_HELPER_MALFORMED_RESPONSE;

    public static final String EVENT = "event_id";
}
