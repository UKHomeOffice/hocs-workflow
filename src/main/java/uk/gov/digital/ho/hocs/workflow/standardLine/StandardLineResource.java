package uk.gov.digital.ho.hocs.workflow.standardLine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class StandardLineResource {

    private final StandardLineService standardLineService;

    @Autowired
    public StandardLineResource(StandardLineService standardLineService) {
        this.standardLineService = standardLineService;
    }
    @GetMapping(value = "/casetype/{caseType}/standardline/{standardLineUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ByteArrayResource> getStandardLineDocument(@PathVariable CaseType caseType, @PathVariable UUID standardLineUUID) throws IOException {
        log.info("get standard line {} for case type - {}", standardLineUUID, caseType);
        return standardLineService.getStandardLineDocument(caseType, standardLineUUID);
    }
}

