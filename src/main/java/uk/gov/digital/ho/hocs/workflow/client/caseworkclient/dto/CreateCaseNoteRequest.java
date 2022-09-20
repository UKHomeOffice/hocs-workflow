package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
public class CreateCaseNoteRequest {

    @NotEmpty
    @JsonProperty(value = "type")
    private String type;

    @NotEmpty
    @JsonProperty(value = "text")
    private String text;

}