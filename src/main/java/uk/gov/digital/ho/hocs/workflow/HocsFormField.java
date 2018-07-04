package uk.gov.digital.ho.hocs.workflow;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class HocsFormField {

    public String component;

    public List<String> validation;

    public Map<String,Object> props;
}
