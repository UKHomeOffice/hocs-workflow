package uk.gov.digital.ho.hocs.workflow.client.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor()
@Getter
@EqualsAndHashCode
public class UnitDto {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private String uuid;

    @JsonProperty("teams")
    private Set<TeamDto> teams;

    @JsonProperty("shortCode")
    private String shortCode;

    @JsonCreator
    public UnitDto(@JsonProperty("displayName") String displayName, @JsonProperty("shortCode") String shortCode) {
        this.displayName = displayName;
        this.uuid = UUID.randomUUID().toString();
        this.shortCode = shortCode;
    }

    public UnitDto(String displayName, String uuid, String shortCode) {
        this.displayName = displayName;
        this.uuid = uuid;
        this.shortCode = shortCode;
    }

}
