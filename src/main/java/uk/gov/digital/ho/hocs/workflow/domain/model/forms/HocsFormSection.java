package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import java.util.ArrayList;
import java.util.List;

public class HocsFormSection {

    public String title;

    public String name;

    public List<HocsFormField> items;

    public HocsFormSection(HocsFormField field) {
        this.title = field.getProps().get("label").toString();
        this.items = new ArrayList();

        if (field.getProps().get("name") != null) {
            this.name = field.getProps().get("name").toString();
        }
    }

}
