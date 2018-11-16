package uk.gov.digital.ho.hocs.workflow.client.infoClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class InfoDeadlines {

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("type")
    private String type;
}
