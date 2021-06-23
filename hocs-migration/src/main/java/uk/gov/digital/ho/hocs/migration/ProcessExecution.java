package uk.gov.digital.ho.hocs.migration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
public class ProcessExecution {

    private String id;
    private String businessKey;
    private String definitionId;

    public String getDefinitionKey() {
        String[] definitionInfo = getDefinitionId().split(":");
        return definitionInfo[0];
    }
}
