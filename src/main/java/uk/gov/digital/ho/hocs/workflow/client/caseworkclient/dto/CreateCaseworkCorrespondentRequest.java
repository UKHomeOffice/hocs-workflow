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

    @JsonProperty("address")
    private AddressDto address;

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
        this.address = new AddressDto(correspondent.getPostcode(), correspondent.getAddress1(), correspondent.getAddress2(), correspondent.getAddress3(),correspondent.getCountry());
        this.telephone = correspondent.getTelephone();
        this.email = correspondent.getEmail();
        this.type = correspondent.getCorrespondentType();
        this.reference = correspondent.getReference();
    }
}
