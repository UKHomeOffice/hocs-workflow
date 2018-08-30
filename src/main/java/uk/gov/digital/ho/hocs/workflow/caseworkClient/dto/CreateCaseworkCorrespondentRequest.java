package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.workflow.model.Correspondent;
import uk.gov.digital.ho.hocs.workflow.model.CorrespondentType;

public class CreateCaseworkCorrespondentRequest {

    @JsonProperty("title")
    private String title;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("surname")
    private String surname;

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

    public CreateCaseworkCorrespondentRequest(Correspondent correspondent) {
        this.title = correspondent.getTitle();
        this.firstName = correspondent.getFirstName();
        this.surname = correspondent.getSurname();
        this.postcode = correspondent.getPostcode();
        this.address1 = correspondent.getAddress1();
        this.address2 = correspondent.getAddress2();
        this.address3 = correspondent.getAddress3();
        this.country = correspondent.getCountry();
        this.telephone = correspondent.getTelephone();
        this.email = correspondent.getEmail();
        this.type = correspondent.getCorrespondentType();
    }
}
