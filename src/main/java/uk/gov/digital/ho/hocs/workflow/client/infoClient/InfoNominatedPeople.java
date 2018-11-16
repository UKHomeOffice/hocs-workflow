package uk.gov.digital.ho.hocs.workflow.client.infoClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class InfoNominatedPeople {


    @JsonProperty("emailAddress")
    private String emailAddress;
}
