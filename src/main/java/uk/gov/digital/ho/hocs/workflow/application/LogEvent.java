package uk.gov.digital.ho.hocs.workflow.application;

public enum LogEvent {
    TASK_COMPLETED,
    CURRENT_STAGE_RETRIEVED,
    TASK_RETRIEVAL_FAILURE,
    CASE_STARTED_SUCCESS,
    CASE_STARTED_FAILURE,
    CASEWORK_CLIENT_CASE_UPDATE_SUCCESS,
    CASEWORK_CLIENT_CASE_UPDATE_FAILURE,
    CASEWORK_CLIENT_STAGE_CREATION_SUCCESS,
    CASEWORK_CLIENT_STAGE_CREATION_FAILURE,
    CASEWORK_CLIENT_CASE_NOTE_CREATION_SUCCESS,
    CASEWORK_CLIENT_CASE_NOTE_CREATION_FAILURE,
    CASEWORK_CLIENT_STAGE_USER_FOUND,
    CASEWORK_CLIENT_STAGE_USER_NOT_FOUND,
    CASEWORK_CLIENT_STAGE_TEAM_UPDATE_SUCCESS,
    CASEWORK_CLIENT_STAGE_TEAM_UPDATE_FAILURE,
    CASEWORK_CLIENT_CASE_TYPE_FOUND,
    CASEWORK_CLIENT_CASE_TYPE_NOT_FOUND,
    CASEWORK_CLIENT_STAGE_FOUND,
    CASEWORK_CLIENT_STAGE_NOT_FOUND,
    CASEWORK_CLIENT_CASE_FOUND,
    CASEWORK_CLIENT_CASE_NOT_FOUND,
    CASEWORK_CLIENT_CREATE_CASE_SUCCESS,
    CASEWORK_CLIENT_CREATE_CASE_FAILURE,
    SECURITY_PARSE_ERROR,
    SECURITY_UNAUTHORISED,
    SECURITY_CASE_NOT_ALLOCATED_TO_USER,
    SECURITY_CASE_NOT_ALLOCATED_TO_TEAM,
    DOCUMENT_CLIENT_CREATE_SUCCESS,
    DOCUMENT_CLIENT_PROCESS_SUCCESS,
    DOCUMENT_CLIENT_FAILURE,
    DOCUMENT_CLIENT_DELETE_SUCCESS,
    INFO_CLIENT_GET_TEAMS_SUCCESS,
    INFO_CLIENT_GET_TEAMS_FAILURE,
    UNCAUGHT_EXCEPTION,
    REST_HELPER_NOT_FOUND,
    REST_HELPER_INTERNAL_SERVER_ERROR,
    REST_HELPER_MALFORMED_RESPONSE,
    CASEWORK_CLIENT_FAILURE;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
