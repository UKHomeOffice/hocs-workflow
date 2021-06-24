package uk.gov.digital.ho.hocs.migration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@AllArgsConstructor
@Getter
public class ProcessVariable {
    final private String type;
    final private Object value;
}
