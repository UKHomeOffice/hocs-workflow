package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Deadline {

    @JsonProperty("type")
    private String type;

    @JsonProperty("date")
    private LocalDate date;
}
