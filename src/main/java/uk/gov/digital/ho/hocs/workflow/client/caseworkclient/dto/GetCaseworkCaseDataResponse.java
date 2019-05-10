package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
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

    @JsonRawValue
    private Map<String,String> data;

    @JsonProperty("primaryTopic")
    private UUID primaryTopic;

    @JsonProperty("primaryCorrespondent")
    private UUID primaryCorrespondent;


}
