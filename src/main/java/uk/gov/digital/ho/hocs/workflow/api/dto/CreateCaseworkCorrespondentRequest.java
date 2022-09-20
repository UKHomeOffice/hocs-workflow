package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class CreateCaseworkCorrespondentRequest {

    @NonNull
    @JsonProperty(value = "type", required = true)
    String type;

    @NonNull
    @NotEmpty
    @JsonProperty(value = "fullname", required = true)
    String fullname;

    @JsonProperty(value = "organisation")
    String organisation;

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
