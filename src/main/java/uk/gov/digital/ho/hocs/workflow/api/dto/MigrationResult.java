package uk.gov.digital.ho.hocs.workflow.api.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor()
@Getter
public class MigrationResult {

  private final List<String> businessKeys;
  private final Map<String, List<String>> before;
  private final Map<String, List<String>> after;
}
