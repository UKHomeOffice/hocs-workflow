package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.workflow.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.workflow.domain.model.CorrespondentType;

import java.util.UUID;

public class CreateCaseworkCorrespondentRequest {

    @JsonProperty("command")
    private String command = "create_correspondent_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("address3")
    private String address3;

    @JsonProperty("country")
    private String country;

    @JsonProperty("telephone")
    private String telephone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("type")
    private CorrespondentType type;

    @JsonProperty("reference")
    private String reference;

    public CreateCaseworkCorrespondentRequest(UUID caseUUID, Correspondent correspondent) {
        this.caseUUID = caseUUID;
        this.fullName = correspondent.getFullname();
        this.postcode = correspondent.getPostcode();
        this.address1 = correspondent.getAddress1();
        this.address2 = correspondent.getAddress2();
        this.address3 = correspondent.getAddress3();
        this.country = correspondent.getCountry();
        this.telephone = correspondent.getTelephone();
        this.email = correspondent.getEmail();
        this.type = correspondent.getCorrespondentType();
        this.reference = correspondent.getReference();
    }
}
