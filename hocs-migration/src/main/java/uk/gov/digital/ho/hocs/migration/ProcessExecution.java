package uk.gov.digital.ho.hocs.migration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@AllArgsConstructor
public class ProcessExecution {

    final private String id;
    final private String businessKey;
    final private String definitionId;

    public String getDefinitionKey() {
        String[] definitionInfo = getDefinitionId().split(":");
        return definitionInfo[0];
    }
}
