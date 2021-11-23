package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HocsFormAccordionTest {

    @Test
    public void shouldHandleEmptyList() {
        List<HocsFormField> fields = new ArrayList();

        List<HocsFormField> result = HocsFormAccordion.loadFormAccordions(fields);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void shouldLoadExpandableCheckboxWithItem() {
        List<HocsFormField> fields = new ArrayList();
        HocsFormField expandable = new HocsFormField();
        expandable.component = "expandable-checkbox";
        expandable.props = new HashMap<String, Object>();
        fields.add(expandable);
        HocsFormField textBox = new HocsFormField();
        textBox.component = "text";
        fields.add(textBox);
        HocsFormField expandableEnd = new HocsFormField();
        expandableEnd.component = "expandable-end";
        fields.add(expandableEnd);
        HocsFormField inset = new HocsFormField();
        inset.component = "inset";
        fields.add(inset);

        List<HocsFormField> result = HocsFormAccordion.loadFormAccordions(fields);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).props.get("items")).isNotNull();
        List<HocsFormField> items = (List<HocsFormField>)result.get(0).props.get("items");
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).component).isEqualTo("text");
        assertThat(result.get(1).component).isEqualTo("inset");
    }

    @Test
    public void shouldLoadAccordionWithExpandableCheckboxWithItem() {
        List<HocsFormField> fields = new ArrayList();
        HocsFormField accordion = new HocsFormField();
        accordion.component = "accordion";
        accordion.props = new HashMap<String, Object>();
        accordion.props.put("label", "test");
        fields.add(accordion);
        HocsFormField expandable = new HocsFormField();
        expandable.component = "expandable-checkbox";
        expandable.props = new HashMap<String, Object>();
        fields.add(expandable);
        HocsFormField textBox = new HocsFormField();
        textBox.component = "text";
        fields.add(textBox);

        List<HocsFormField> result = HocsFormAccordion.loadFormAccordions(fields);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).props.get("sections")).isNotNull();
        List<HocsFormSection> sections = (ArrayList)result.get(0).props.get("sections");
        assertThat(sections.size()).isEqualTo(1);
        assertThat(sections.get(0).items.size()).isEqualTo(1);
        assertThat(sections.get(0).items.get(0).props.get("items")).isNotNull();
        List<HocsFormField> items = (List<HocsFormField>)sections.get(0).items.get(0).props.get("items");
        assertThat(items.size()).isEqualTo(1);
    }

    @Test
    public void shouldLoadAccordionWithGivenNameIfNameProvided() {
        String expected = "nameResult";

        List<HocsFormField> fields = new ArrayList();
        HocsFormField accordion = new HocsFormField();
        accordion.component = "accordion";
        accordion.props = new HashMap<String, Object>();
        accordion.props.put("label", "test");
        accordion.props.put("name", expected);
        fields.add(accordion);
        HocsFormField expandable = new HocsFormField();
        expandable.component = "expandable-checkbox";
        expandable.props = new HashMap<String, Object>();
        fields.add(expandable);
        HocsFormField textBox = new HocsFormField();
        textBox.component = "text";
        fields.add(textBox);

        List<HocsFormField> result = HocsFormAccordion.loadFormAccordions(fields);
        List<HocsFormSection> sections = (ArrayList)result.get(0).props.get("sections");
        String nameResult = sections.get(0).name;
        assertThat(nameResult).isEqualTo(expected);
    }
}
