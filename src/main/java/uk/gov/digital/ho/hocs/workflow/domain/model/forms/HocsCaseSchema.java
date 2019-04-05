package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class HocsCaseSchema {

    @JsonProperty("title")
    public String title;

    @JsonProperty("fields")
    public Map<String, List<HocsFormField>> fields;

}