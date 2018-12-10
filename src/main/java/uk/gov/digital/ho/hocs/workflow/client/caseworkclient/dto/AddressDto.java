package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.Correspondent;

@AllArgsConstructor
@Getter
public class AddressDto {


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


    public static AddressDto from(Correspondent correspondent) {
        return new AddressDto(
                correspondent.getPostcode(),
                correspondent.getAddress1(),
                correspondent.getAddress2(),
                correspondent.getAddress3(),
                correspondent.getCountry()
        );
    }
}
