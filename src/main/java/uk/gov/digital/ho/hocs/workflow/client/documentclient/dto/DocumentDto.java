package uk.gov.digital.ho.hocs.workflow.client.documentclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor()
@Getter
public class DocumentDto {

    @JsonProperty("displayName")
    private String displayName;
}
