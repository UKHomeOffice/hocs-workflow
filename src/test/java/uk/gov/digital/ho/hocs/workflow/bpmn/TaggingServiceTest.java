package uk.gov.digital.ho.hocs.workflow.bpmn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaggingServiceTest {
    @Mock
    private CaseworkClient caseworkClient;
    private TaggingService taggingService;

    private final UUID caseUUID = UUID.randomUUID();

    @Before
    public void setup() {
        taggingService = new TaggingService(caseworkClient);
    }

    @Test
    public void shouldCreateTagForCase(){
        taggingService.createTagForCase(caseUUID.toString(), "TEST_TAG");
        verify(caseworkClient).createCaseTag(caseUUID, "TEST_TAG");
        verifyNoMoreInteractions(caseworkClient);
    }
}
