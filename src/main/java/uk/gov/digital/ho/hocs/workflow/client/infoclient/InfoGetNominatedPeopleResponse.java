package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public class InfoGetNominatedPeopleResponse {

    @JsonProperty("nominatedPeople")
    private Set<InfoNominatedPeople> nominatedPeople;
}
