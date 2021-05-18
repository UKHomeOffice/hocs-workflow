package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class HocsSchema {

    @JsonProperty("title")
    public String title;

    @JsonProperty("defaultActionLabel")
    public String defaultActionLabel;

    @JsonProperty("fields")
    public List<HocsFormField> fields;

    @JsonProperty("secondaryActions")
    private List<HocsFormSecondaryAction> secondaryActions;

    @JsonProperty("props")
    private Object props;
}