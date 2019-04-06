package uk.gov.digital.ho.hocs.workflow.client.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class TeamDto implements Serializable {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private UUID uuid;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("permissions")
    private Set<PermissionDto> permissionDtos;


    public TeamDto() {
        permissionDtos = new HashSet<>();
    }

    public TeamDto(String displayName, UUID uuid, boolean active, Set<PermissionDto> permissionDtos) {
        this.displayName = displayName;
        this.uuid = uuid;
        this.active = active;
        this.permissionDtos = permissionDtos;
    }
}
