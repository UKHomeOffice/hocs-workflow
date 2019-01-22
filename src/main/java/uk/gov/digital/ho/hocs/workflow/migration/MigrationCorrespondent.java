package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MigrationCorrespondent {

    @JsonProperty(value = "type")
    String type;

    @JsonProperty(value = "title")
    String title;

    @JsonProperty(value = "forename")
    String forename;

    @JsonProperty(value = "surname")
    String surname;

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

    @JsonProperty("isPrimary")
    Boolean isPrimary;

    @Setter
    UUID uuid;
}
