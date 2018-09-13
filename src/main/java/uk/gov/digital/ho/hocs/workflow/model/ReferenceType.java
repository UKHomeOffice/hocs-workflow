package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum ReferenceType {

   MEMBER_REFERENCE("MP Reference"),
   HO_REFERENCE("HO Reference"),
   PASSPORT_NUMBER("Passport Number"),
   CID_NUMBER("CID Number"),
   APPLICATION_NUMBER("Application Number"),
   CORESPONDENT_REFERENCE("Correspondent Reference"),
   OTHER("Other");

    @Getter
    private String displayValue;

    ReferenceType(String value){
        displayValue = value;
    }
}
