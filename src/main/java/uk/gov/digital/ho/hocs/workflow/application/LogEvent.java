package uk.gov.digital.ho.hocs.workflow.application;

public enum LogEvent {
    TASK_COMPLETED,
    CURRENT_STAGE_RETRIEVED,
    TASK_RETRIEVAL_FAILURE,
    CASE_STARTED_SUCCESS,
    CASE_STARTED_FAILURE,
    CASE_NOT_FOUND,
    CREATE_CASE_SUCCESS,
    SECURITY_PARSE_ERROR,
    SECURITY_UNAUTHORISED,
    SECURITY_CASE_NOT_ALLOCATED_TO_USER,
    SECURITY_CASE_NOT_ALLOCATED_TO_TEAM,
    DOCUMENT_CLIENT_CREATE_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPES_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS,
    INFO_CLIENT_GET_SCHEMAS_SUCCESS,
    INFO_CLIENT_GET_FORM_SUCCESS,
    INFO_CLIENT_GET_TEAMS_SUCCESS,
    INFO_CLIENT_GET_TEAM_SUCCESS,
    INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS,
    INFO_CLIENT_GET_TEAM_FOR_TOPIC_STAGE_SUCCESS,
    INFO_CLIENT_GET_USER_SUCESS,
    INFO_CLIENT_GET_UNIT_FOR_TEAM_SUCESS,
    INFO_CLIENT_GET_CASE_DETAILS_FIELDS,
    INFO_CLIENT_CALCULATE_DEADLINE,
    UNCAUGHT_EXCEPTION,
    REST_HELPER_POST,
    REST_HELPER_POST_FAILURE,
    REST_HELPER_PUT,
    REST_HELPER_GET,
    REST_HELPER_GET_UNAUTHORIZED,
    REST_HELPER_GET_FORBIDDEN,
    REST_HELPER_GET_NOT_FOUND,
    REST_HELPER_GET_BAD_REQUEST,
    MIGRATION_EVENT,
    CASE_CLOSE_ERROR,
    WORKFLOW_SERVICE_UPDATE_CASE_DATA_VALUES,
    AUTH_FILTER_FAILURE,
    AUTH_FILTER_SUCCESS,
    CASE_NOTE_FAILED,
    STAGE_CREATION_STARTED,
    STAGE_CREATION_SUCCESS,
    AUDIT_FAILED,
    AUDIT_EVENT_CREATED,
    CASE_CREATE_FAILURE;

    public static final String EVENT = "event_id";

    public static final String EXCEPTION = "exception";
}
