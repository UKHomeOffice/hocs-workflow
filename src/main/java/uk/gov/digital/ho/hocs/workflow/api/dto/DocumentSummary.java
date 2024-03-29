package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DocumentSummary {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("s3UntrustedUrl")
    private String s3UntrustedUrl;

}
