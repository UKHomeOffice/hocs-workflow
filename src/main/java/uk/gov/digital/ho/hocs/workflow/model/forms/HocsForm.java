package uk.gov.digital.ho.hocs.workflow.model.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class HocsForm {

    @JsonProperty("schema")
    private HocsSchema schema;

    @Setter
    @JsonProperty("data")
    private Map<String,String> data;

    public HocsForm(HocsSchema schema) {
        this.schema = schema;
        this.data = new HashMap<>();
    }

}
