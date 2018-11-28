package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkCaseRequestTest {

    @Test
    public void getCreateCaseRequest() {

        CaseDataType caseDataType = CaseDataType.MIN;
        Map<String, String> data = new HashMap<>();
        LocalDate deadline = LocalDate.now();

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(caseDataType, data, deadline);

        assertThat(createCaseRequest.getType()).isEqualTo(caseDataType);
        assertThat(createCaseRequest.getData()).isEqualTo(data);
        assertThat(createCaseRequest.getCaseDeadline()).isEqualTo(deadline);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(null, null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getData()).isNull();
        assertThat(createCaseRequest.getCaseDeadline()).isNull();

    }
}