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
import uk.gov.digital.ho.hocs.workflow.api.MigrationToolsService.CaseExecution;
import uk.gov.digital.ho.hocs.workflow.api.MigrationToolsService.MigrationCompare;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrateResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationRequest;

@RestController
@Slf4j
public class MigrationToolsResource {

  private final MigrationToolsService migrationToolsService;

  @Autowired
  public MigrationToolsResource(MigrationToolsService migrationToolsService) {
    this.migrationToolsService = migrationToolsService;
  }

  @PostMapping(value = "/tool/migrate", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<MigrateResponse> migrate(@RequestBody MigrationRequest migrationRequest) {
    List<String> businessKeys = migrationToolsService.migrate(migrationRequest);
    return ResponseEntity.ok( new MigrateResponse(businessKeys));
  }

  @GetMapping(value = "/tool/execution/{executionUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<CaseExecution> getExecution(@PathVariable UUID executionUuid) {
    CaseExecution caseExecution = migrationToolsService.getExecution(executionUuid);
    return ResponseEntity.ok(caseExecution);
  }

  @GetMapping(value = "/tool/report/{caseUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<MigrationCompare> report(@PathVariable UUID caseUuid) {
    MigrationCompare response = migrationToolsService.report(caseUuid);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/tool/processDefinition/{processDefinitionKey}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Map<String, List<String>>> diagramsKey(@PathVariable String processDefinitionKey) {
    Map<String, List<String>> executionKeyMap = migrationToolsService.diagramsKey(processDefinitionKey);
    return ResponseEntity.ok(executionKeyMap);
  }

  @GetMapping(value = "/tool/processDefinition/{processDefinitionKey}/counts", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Map<String, Integer>> diagramsCounts(@PathVariable String processDefinitionKey) {
    Map<String, Integer> executionCounts = migrationToolsService.diagramsCounts(processDefinitionKey);
    return ResponseEntity.ok(executionCounts);
  }
}
