package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateCaseNoteRequest {

    @JsonProperty(value="type")
    private String type;

    @JsonProperty(value="text")
    private String text;

}