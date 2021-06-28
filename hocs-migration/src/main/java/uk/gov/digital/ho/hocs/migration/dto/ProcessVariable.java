package uk.gov.digital.ho.hocs.migration.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ProcessVariable {
    private String type;
    private Object value;
}
