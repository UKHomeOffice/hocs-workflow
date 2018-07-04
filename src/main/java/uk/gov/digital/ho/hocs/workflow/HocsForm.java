package uk.gov.digital.ho.hocs.workflow;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class HocsForm {

    public HocsSchema schema;

    public Map<String,Object> data;

}
