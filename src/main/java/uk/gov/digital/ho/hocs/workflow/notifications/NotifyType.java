package uk.gov.digital.ho.hocs.workflow.notifications;

import lombok.Getter;

public enum NotifyType {

    DATA_INPUT_REJECT("50223419-1fba-4651-93e2-e03613c4a128"),
    INITIAL_DRAFT_REJECT("00a6ef7d-2f37-4978-8eaf-83a0e6e15c60"),
    QA_REJECT("848e631c-8223-4043-9a36-29263bcf9278"),
    PRIVATE_OFFICE_REJECT("279adc21-875f-4972-8153-b7b44660f19e"),
    MINISTER_REJECT("c97dad2b-ea1d-4e54-815b-2d4205429dc6"),
    DISPATCH_REJECT("5f11b96a-fe62-40a2-90a9-b0c2da0a55bf"),
    ALLOCATE_TEAM("49613c2c-7f1b-42c1-8947-a63106ab7f81"),
    ALLOCATE_INDIVIDUAL("d3566a59-f23c-446f-b9fd-ad9c78e16abb");

    @Getter
    private String displayValue;

    NotifyType(String value){
        displayValue = value;
    }
}
