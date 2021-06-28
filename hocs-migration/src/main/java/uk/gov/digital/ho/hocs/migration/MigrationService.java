package uk.gov.digital.ho.hocs.migration;

import java.io.IOException;
import java.util.List;

public interface MigrationService {
    void fixCase(String caseUuid) throws IOException;

    List<String> getFixableCases();

    String getDescription();
}
