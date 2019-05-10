package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class GetFullCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z", timezone = "UTC")
    private ZonedDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("primaryTopic")
    private GetTopicResponse primaryTopic;

    @JsonProperty("primaryCorrespondent")
    private GetCorrespondentResponse primaryCorrespondent;

}


