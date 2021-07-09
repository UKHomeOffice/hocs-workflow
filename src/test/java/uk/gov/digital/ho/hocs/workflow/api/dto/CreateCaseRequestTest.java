package uk.gov.digital.ho.hocs.workflow.api.dto;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseRequestTest {
    @Test
    public void getCreateCaseRequest() {

        String caseDataType = "MIN";
        LocalDate dateReceived = LocalDate.now();
        List<DocumentSummary> documentSummaryList = new ArrayList<>();
        Map<String, String> data = new HashMap<>();
        data.put("TestKey", "TestValue");

        CreateCaseRequest createCaseRequest = new CreateCaseRequest(caseDataType, dateReceived, data, documentSummaryList);

        assertThat(createCaseRequest.getType()).isEqualTo(caseDataType);
        assertThat(createCaseRequest.getDateReceived()).isEqualTo(dateReceived);
        assertThat(createCaseRequest.getData()).isEqualTo(data);
        assertThat(createCaseRequest.getDocuments()).isEqualTo(documentSummaryList);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseRequest createCaseRequest = new CreateCaseRequest(null, null, null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getDateReceived()).isNull();
        assertThat(createCaseRequest.getData()).isNull();
        assertThat(createCaseRequest.getDocuments()).isNull();

    }
}