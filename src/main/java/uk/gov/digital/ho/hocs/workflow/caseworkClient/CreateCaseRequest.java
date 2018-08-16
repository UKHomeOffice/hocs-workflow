package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("type")
    private CaseType type;
}
