package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CorrespondentType;

@NoArgsConstructor
@Getter
public class AddCorrespondentRequest {

    @JsonProperty("type")
    CorrespondentType type;
    @JsonProperty("fullname")
    String fullname;
    @JsonProperty("postcode")
    String postcode;
    @JsonProperty("address1")
    String address1;
    @JsonProperty("address2")
    String address2;
    @JsonProperty("address3")
    String address3;
    @JsonProperty("country")
    String country;
    @JsonProperty("telephone")
    String telephone;
    @JsonProperty("email")
    String email;
    @JsonProperty("reference")
    String reference;

}
