package uk.gov.digital.ho.hocs.workflow.client.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.security.AccessLevel;

@Getter
@EqualsAndHashCode
public class PermissionDto {

    @JsonCreator
    public PermissionDto(@JsonProperty("caseTypeCode") String caseTypeCode, @JsonProperty("accessLevel") AccessLevel accessLevel) {
        this.caseTypeCode = caseTypeCode;
        this.accessLevel = accessLevel;
    }
    @JsonProperty("caseTypeCode")
    private String caseTypeCode;

    @JsonProperty("accessLevel")
    private AccessLevel accessLevel;

}
