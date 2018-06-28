package uk.gov.digital.ho.hocs.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseResponse {

    private final String caseReference;

    private final UUID uuid;

}
