package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class GetCaseworkCaseDataResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("reference")
    private String reference;

    @JsonRawValue
    private Map<String,String> data;

    @JsonProperty("primaryTopic")
    private String primaryTopic;

    @JsonProperty("primaryCorrespondent")
    private String primaryCorrespondent;


}
