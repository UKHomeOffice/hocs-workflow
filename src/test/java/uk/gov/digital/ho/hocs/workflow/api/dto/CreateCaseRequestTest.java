package uk.gov.digital.ho.hocs.workflow.api.dto;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseRequestTest {
    @Test
    public void getCreateCaseRequest() {

        String caseDataType = "MIN";
        LocalDate dateReceived = LocalDate.now();
        List<DocumentSummary> documentSummaryList = new ArrayList<>();

        CreateCaseRequest createCaseRequest = new CreateCaseRequest(caseDataType, dateReceived, documentSummaryList, null);

        assertThat(createCaseRequest.getType()).isEqualTo(caseDataType);
        assertThat(createCaseRequest.getDateReceived()).isEqualTo(dateReceived);
        assertThat(createCaseRequest.getDocuments()).isEqualTo(documentSummaryList);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseRequest createCaseRequest = new CreateCaseRequest(null, null, null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getDateReceived()).isNull();
        assertThat(createCaseRequest.getDocuments()).isNull();

    }
}