package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.domain.model.HocsCaseUUID;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkCaseRequestTest {

    @Test
    public void getCreateCaseRequest() {

        CaseDataType caseDataType = CaseDataType.MIN;
        CaseType caseType = new CaseType(caseDataType.getDisplayName(), caseDataType.getValue());
        Map<String, String> data = new HashMap<>();

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(caseType, data);

        assertThat(createCaseRequest.getType()).isEqualTo(caseType);
        assertThat(createCaseRequest.getData()).isEqualTo(data);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getData()).isNull();

    }
}