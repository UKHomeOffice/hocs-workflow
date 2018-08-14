package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum StageType {

    RUSH_ONLY_STAGE("Stage"),

    DCU_MIN_DATA_INPUT("Data Input"),
    DCU_MIN_DATA_INPUT_QA("Data Input QA"),
    DCU_MIN_MARKUP("Markup"),
    DCU_MIN_TRANSFER_CONFIRMATION("Transfer Confirmation"),
    DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION("No Reply Needed Confirmation"),
    DCU_MIN_INITIAL_DRAFT("Initial Draft"),
    DCU_MIN_QA_RESPONSE("QA Response"),
    DCU_MIN_PRIVATE_OFFICE("Private Office"),
    DCU_MIN_MINISTER_SIGN_OFF("Minister Sign Off"),
    DCU_MIN_DISPATCH("Dispatch");

    @Getter
    private String displayValue;

    StageType(String value){
        displayValue = value;
    }
}
