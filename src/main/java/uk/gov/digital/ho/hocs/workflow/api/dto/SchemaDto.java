package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor()
@NoArgsConstructor()
@Getter
public class SchemaDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("defaultActionLabel")
    private String defaultActionLabel;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("fields")
    private List<FieldDto> fields;

    @JsonProperty("secondaryActions")
    private List<SecondaryActionDto> secondaryActions;

    @JsonProperty("props")
    private Object props;

    @JsonProperty("validation")
    private Object validation;

}
