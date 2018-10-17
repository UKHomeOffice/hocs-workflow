package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum StageName {

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
    DCU_MIN_DISPATCH("Dispatch"),
    DCU_MIN_COPY_NUMBER_TEN("Copy to Number Ten"),
    DCU_TRO_DATA_INPUT("Data Input"),
    DCU_TRO_DATA_INPUT_QA("Data Input QA"),
    DCU_TRO_MARKUP("Markup"),
    DCU_TRO_TRANSFER_CONFIRMATION("Transfer Confirmation"),
    DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION("No Reply Needed Confirmation"),
    DCU_TRO_INITIAL_DRAFT("Initial Draft"),
    DCU_TRO_QA_RESPONSE("QA Response"),
    DCU_TRO_DISPATCH("Dispatch"),
    DCU_TRO_COPY_NUMBER_TEN("Copy to Number Ten"),
    DCU_DTEN_DATA_INPUT("Data Input"),
    DCU_DTEN_DATA_INPUT_QA("Data Input QA"),
    DCU_DTEN_MARKUP("Markup"),
    DCU_DTEN_TRANSFER_CONFIRMATION("Transfer Confirmation"),
    DCU_DTEN_NO_REPLY_NEEDED_CONFIRMATION("No Reply Needed Confirmation"),
    DCU_DTEN_INITIAL_DRAFT("Initial Draft"),
    DCU_DTEN_QA_RESPONSE("QA Response"),
    DCU_DTEN_PRIVATE_OFFICE("Private Office"),
    DCU_DTEN_DISPATCH("Dispatch");

    @Getter
    private String displayValue;

    StageName(String value){
        displayValue = value;
    }

}
