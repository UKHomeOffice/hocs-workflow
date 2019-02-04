package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TeamDto implements Serializable {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private UUID uuid;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("permissionDtos")
    private Set<PermissionDto> permissionDtos;

}
