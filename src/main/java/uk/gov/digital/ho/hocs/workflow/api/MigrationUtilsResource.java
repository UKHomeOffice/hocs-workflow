package uk.gov.digital.ho.hocs.workflow.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.dto.ProcessDefinitionCheckSumSummary;

@RestController
@RequestMapping("migration-utils")
@Slf4j
public class MigrationUtilsResource {

  private final MigrationUtilsService migrationUtilsService;

  @Autowired
  public MigrationUtilsResource(MigrationUtilsService migrationUtilsService) {
    this.migrationUtilsService = migrationUtilsService;
  }

  @GetMapping(value = "/processDefinitionExplorer/{processDefinitionKey}", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<List<ProcessDefinitionCheckSumSummary>> diagramsKey(@PathVariable String processDefinitionKey, @RequestParam("changes") Optional<Boolean> optionalChanges) throws IOException {
    boolean changes = optionalChanges.isPresent() && optionalChanges.get();
    List<ProcessDefinitionCheckSumSummary> executionKeyMap = migrationUtilsService.processDefinitionChecksums(processDefinitionKey, changes);
    return ResponseEntity.ok(executionKeyMap);
  }
}
