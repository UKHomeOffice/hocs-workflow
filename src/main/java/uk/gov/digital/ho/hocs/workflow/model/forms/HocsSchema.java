package uk.gov.digital.ho.hocs.workflow.model.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class HocsSchema {

    @JsonProperty("action")
    public HocsFormAction action;

    @JsonProperty("title")
    public String title;

    @JsonProperty("defaultActionLabel")
    public String defaultActionLabel;

    @JsonProperty("fields")
    public List<HocsFormField> fields;
}