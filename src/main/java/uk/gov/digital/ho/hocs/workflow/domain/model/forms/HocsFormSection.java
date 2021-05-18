package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import java.util.ArrayList;
import java.util.List;

public class HocsFormSection {

    public String title;

    public List<HocsFormField> items;

    public HocsFormSection(HocsFormField field) {
        this.title = field.getProps().get("label").toString();
        this.items = new ArrayList();
    }
}
