package uk.gov.digital.ho.hocs.migration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ProcessVariable {

    @Getter
    private String type;

    @Getter
    private Object value;

    public ProcessVariable() {
    }


}
