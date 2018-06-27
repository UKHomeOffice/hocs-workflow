package uk.gov.digital.ho.hocs.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateStageResponse {

    private final UUID uuid;
}