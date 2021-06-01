package uk.gov.digital.ho.hocs.workflow.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.workflow.api.MigrationService.MigrationResult;
import uk.gov.digital.ho.hocs.workflow.api.MigrationService.Report;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationRequest;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaMigrationClient.CaseExecution;

@RestController
@Slf4j
public class MigrationToolsResource {

  private final MigrationService migrationService;

  @Autowired
  public MigrationToolsResource(MigrationService migrationService) {
    this.migrationService = migrationService;
  }

  @PostMapping(value = "/tool/migrate", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<MigrationResult> migrate(@RequestBody MigrationRequest migrationRequest) {
    MigrationResult migrationResult = migrationService.migrate(migrationRequest);
    return ResponseEntity.ok(migrationResult);
  }

  @GetMapping(value = "/tool/execution/{executionUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<CaseExecution> getExecution(@PathVariable UUID executionUuid) {
    CaseExecution caseExecution = migrationService.getExecution(executionUuid);
    return ResponseEntity.ok(caseExecution);
  }

  @GetMapping(value = "/tool/report/{caseUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Report> report(@PathVariable UUID caseUuid) {
    Report response = migrationService.report(caseUuid);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/tool/processDefinition/{processDefinitionKey}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Map<String, List<String>>> diagramsKey(@PathVariable String processDefinitionKey) {
    Map<String, List<String>> executionKeyMap = migrationService.diagramsKey(processDefinitionKey);
    return ResponseEntity.ok(executionKeyMap);
  }

  @GetMapping(value = "/tool/processDefinition/{processDefinitionKey}/counts", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Map<String, Integer>> diagramsCounts(@PathVariable String processDefinitionKey) {
    Map<String, Integer> executionCounts = migrationService.diagramsCounts(processDefinitionKey);
    return ResponseEntity.ok(executionCounts);
  }
}
