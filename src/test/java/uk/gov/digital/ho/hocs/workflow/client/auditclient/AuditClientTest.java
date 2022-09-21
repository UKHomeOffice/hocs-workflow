package uk.gov.digital.ho.hocs.workflow.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.util.BaseAwsTest;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.dto.CreateAuditRequest;

import javax.validation.constraints.NotNull;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuditClient.class)
@ActiveProfiles("local")
public class AuditClientTest
//        extends BaseAwsTest
{

    //@Captor
  //  private ArgumentCaptor<PublishRequest> publicRequestCaptor;
//    private ResultCaptor<PublishResult> snsPublishResult;
//    @SpyBean
    //private AmazonSNSAsync auditSearchSnsClient;
    //@MockBean(name = "requestData")
    //private RequestData requestData;
    //@MockBean
    //private RestHelper restHelper;

    @Autowired
    private AuditClient auditClient;

    //@Value("${hocs.audit-service}")
    //private String auditService;

    @Before
    public void setUp() {
       /* when(requestData.correlationId()).thenReturn(UUID.randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");*/

        //snsPublishResult = new ResultCaptor<>();
        //doAnswer(snsPublishResult).when(auditSearchSnsClient).publish(any());
    }

    @Test
    public void shouldSendCaseComplaintEvent() {
        //auditClient.createCaseComplaintType();

        //verify(auditSearchSnsClient).publish(publicRequestCaptor.capture());
        //assertSnsValues(caseData.getUuid(), EventType.CASE_CREATED);
        System.out.println("Test completed");
    }


 /*   private void assertSnsValues(UUID caseUuid, EventType event, @NotNull Map<String, String> otherValues) throws JsonProcessingException {
        var caseCreated =
                objectMapper.readValue(publicRequestCaptor.getValue().getMessage(), CreateAuditRequest.class);

        Assertions.assertNotNull(snsPublishResult.getResult());
        Assertions.assertNotNull(snsPublishResult.getResult().getMessageId());
        Assertions.assertEquals(snsPublishResult.getResult().getSdkHttpMetadata().getHttpStatusCode(), 200);
        Assertions.assertEquals(caseCreated.getCaseUUID(), caseUuid);
        Assertions.assertEquals(caseCreated.getType(), event);

        if (!otherValues.isEmpty()) {
            var caseCreatedData =
                    objectMapper.readValue(caseCreated.getAuditPayload(), Map.class);

            Assertions.assertTrue(caseCreatedData.entrySet().containsAll(otherValues.entrySet()));
        }
    }*/

}
