package uk.gov.digital.ho.hocs.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class CreateCaseResponse {

    private String caseReference;

    private UUID uuid;

}
