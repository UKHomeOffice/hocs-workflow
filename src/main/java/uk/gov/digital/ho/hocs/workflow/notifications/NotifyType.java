package uk.gov.digital.ho.hocs.workflow.notifications;

import lombok.Getter;

public enum NotifyType {

    INITIAL_DRAFT_REJECT("3a47cd64-2ebb-411d-8194-fd384f377ccc"),
    QA_REJECT("f604a555-9a1a-40ec-8a60-61fb495630e9"),
    PRIVATE_OFFICE_REJECT("f432a529-1b1b-49af-976d-ce23e745e474"),
    MINISTER_REJECT("78df9bff-9cb0-414a-bb82-32e26847cc5a"),
    DISPATCH_REJECT("0912ba97-1043-41c0-b583-68d6e65f3de7"),
    ALLOCATE_TEAM("f9716987-395e-453f-9a59-9a8a47f23152"),
    ALLOCATE_INDIVIDUAL("3dfbd276-2bcc-4b08-81b1-d4f0583cdf39"),
    NRN_REJECT("8d4f8da4-a646-468b-8e91-3063c12ae812"),
    TRANSFER_OGD_REJECT("d860dd8a-6873-4b07-be85-022aa505a9e2");

    @Getter
    private String displayValue;

    NotifyType(String value){
        displayValue = value;
    }
}
