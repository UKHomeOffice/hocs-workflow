package uk.gov.digital.ho.hocs.workflow.domain.repositories.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Schema {

    @JsonProperty("title")
    private String title;

    @JsonProperty("defaultActionLabel")
    private String defaultActionLabel;

    @JsonProperty("fields")
    private List<Field> fields;

    @JsonProperty("secondaryActions")
    private List<SecondaryAction> secondaryActions;

    @JsonProperty("props")
    private Object props;

    @JsonProperty("validation")
    private Object validation;

    @JsonProperty("summary")
    private Object summary;

}
