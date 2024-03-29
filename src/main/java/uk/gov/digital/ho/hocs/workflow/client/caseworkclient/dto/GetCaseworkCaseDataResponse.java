package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor()
@NoArgsConstructor
@Data
@Getter
public class GetCaseworkCaseDataResponse {

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

    @JsonProperty("primaryTopicUUID")
    private UUID primaryTopicUUID;

    @JsonProperty("primaryTopic")
    private GetTopicResponse primaryTopic;

    @JsonProperty("primaryCorrespondentUUID")
    private UUID primaryCorrespondentUUID;

    @JsonProperty("primaryCorrespondent")
    private GetCorrespondentResponse primaryCorrespondent;

    @JsonProperty("completed")
    private Boolean completed;

}


