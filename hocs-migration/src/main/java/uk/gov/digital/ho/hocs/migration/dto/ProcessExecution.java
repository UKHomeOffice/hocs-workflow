package uk.gov.digital.ho.hocs.migration.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessExecution {

    private String id;
    private String businessKey;
    private String definitionId;

    public String getDefinitionKey() {
        String[] definitionInfo = getDefinitionId().split(":");
        return definitionInfo[0];
    }
}
