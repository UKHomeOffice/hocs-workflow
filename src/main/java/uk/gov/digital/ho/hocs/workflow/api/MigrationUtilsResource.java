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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.MigrationResult;
import uk.gov.digital.ho.hocs.workflow.api.dto.VariableChangeRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.VariableReport;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.CaseExecution;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.CaseTask;

@RestController
@RequestMapping("migration-utils")
@Slf4j
public class MigrationUtilsResource {

  private final MigrationUtilsService migrationUtilsService;

  @Autowired
  public MigrationUtilsResource(MigrationUtilsService migrationUtilsService) {
    this.migrationUtilsService = migrationUtilsService;
  }

  @PostMapping(value = "/migrate", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<MigrationResult> migrate(@RequestBody MigrationRequest migrationRequest) {
    MigrationResult migrationResult = migrationUtilsService.migrateWithMapEqualActivities(migrationRequest);
    return ResponseEntity.ok(migrationResult);
  }

  @PostMapping(value = "/execution/{executionUuid}/variable", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity setExecutionVariable(@PathVariable UUID executionUuid, @RequestBody VariableChangeRequest variableChangeRequest) {
    migrationUtilsService.setExecutionVariable(executionUuid, variableChangeRequest.getVariable(), variableChangeRequest.getValue());
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/task/{taskUuid}/variable", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity setTaskVariable(@PathVariable UUID taskUuid, @RequestBody VariableChangeRequest variableChangeRequest) {
    migrationUtilsService.setTaskVariable(taskUuid, variableChangeRequest.getVariable(), variableChangeRequest.getValue());
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/execution/{executionUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<CaseExecution> getExecution(@PathVariable UUID executionUuid) {
    CaseExecution caseExecution = migrationUtilsService.getExecution(executionUuid);
    return ResponseEntity.ok(caseExecution);
  }

  @GetMapping(value = "/task/{taskUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<CaseTask> getTask(@PathVariable UUID taskUuid) {
    CaseTask task = migrationUtilsService.getTask(taskUuid);
    return ResponseEntity.ok(task);
  }

  @GetMapping(value = "/report/{caseUuid}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<VariableReport> report(@PathVariable UUID caseUuid) {
    VariableReport response = migrationUtilsService.report(caseUuid);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/processDefinition/{processDefinitionKey}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Map<String, List<String>>> diagramsKey(@PathVariable String processDefinitionKey) {
    Map<String, List<String>> executionKeyMap = migrationUtilsService.diagramsKey(processDefinitionKey);
    return ResponseEntity.ok(executionKeyMap);
  }

  @GetMapping(value = "/processDefinition/{processDefinitionKey}/counts", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Map<String, Integer>> diagramsCounts(@PathVariable String processDefinitionKey) {
    Map<String, Integer> executionCounts = migrationUtilsService.diagramsCounts(processDefinitionKey);
    return ResponseEntity.ok(executionCounts);
  }
}
