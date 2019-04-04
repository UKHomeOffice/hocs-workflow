package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkCaseRequestTest {

    @Test
    public void getCreateCaseRequest() {

        CaseDataType caseDataType = CaseDataType.MIN;
        CaseType caseType = new CaseType(caseDataType.getType(), caseDataType.getShortCode(), caseDataType.getType());
        Map<String, String> data = new HashMap<>();
        LocalDate dateReceived = LocalDate.now().minusDays(4);

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(caseType, data, dateReceived);

        assertThat(createCaseRequest.getType()).isEqualTo(caseType);
        assertThat(createCaseRequest.getData()).isEqualTo(data);
        assertThat(createCaseRequest.getDateReceived()).isEqualTo(dateReceived);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(null, null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getData()).isNull();
        assertThat(createCaseRequest.getDateReceived()).isNull();

    }
}