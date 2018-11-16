package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.client.documentClient.model.DocumentType;

@NoArgsConstructor
@Getter
public class DocumentSummary {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private DocumentType type;

    @JsonProperty("s3UntrustedUrl")
    private String s3UntrustedUrl;
}
