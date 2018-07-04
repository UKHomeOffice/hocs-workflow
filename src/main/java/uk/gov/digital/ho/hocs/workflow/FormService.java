package uk.gov.digital.ho.hocs.workflow;


import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FormService {

    private static Map<String,HocsForm> forms = new HashMap<>();

    static {
        forms.put("INITIAL_DECISION", getInitialDecision());
        forms.put("DEADLINES", getDeadlinesForm());
        forms.put("TOPICS", getTopics());
        forms.put("ANSWERING",getAllocateForm());
        forms.put("ALLOCATION_NOTE",getAllocationNote());
    }

    public HocsForm getStage(String form){
        return forms.get(form);
    }

    private static HocsForm getInitialDecision() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Test1");
        choice1.put("value", "Test1");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Test2");
        choice2.put("value", "Test2");

        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Test3");
        choice3.put("value", "Test3");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "InitialDecision");
        properties4.put("label", "Initial Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Initial Decision", "Default", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;
    }

    private static HocsForm getAllocateForm() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "MarkupUnit");
        properties4.put("label", "Markup Unit");

        HocsFormField fieldFour = new HocsFormField("dropdown", validationList, properties4);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("name", "MarkupTeam");
        properties5.put("label", "Markup Team");

        HocsFormField fieldFive = new HocsFormField("dropdown", validationList, properties5);

        Map<String,Object> properties6 = new HashMap<>();
        properties6.put("name", "SignOffMinister");
        properties6.put("label", "Sign-Off Minister");

        HocsFormField fieldSix = new HocsFormField("dropdown", validationList, properties6);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);
        formFields.add(fieldSix);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Answering", "Default", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;
    }

    private static HocsForm getDeadlinesForm() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties1 = new HashMap<>();
        properties1.put("name", "DateReceived");
        properties1.put("label", "Date Received");

        HocsFormField fieldOne = new HocsFormField("date", validationList, properties1);

        Map<String,Object> properties2 = new HashMap<>();
        properties2.put("name", "DraftingDeadline");
        properties2.put("label", "Drafting Deadline");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String,Object> properties3 = new HashMap<>();
        properties3.put("name", "FinalDeadline");
        properties3.put("label", "Final Deadline");

        HocsFormField fieldThree = new HocsFormField("date", validationList, properties3);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldOne);
        formFields.add(fieldTwo);
        formFields.add(fieldThree);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Deadlines", "Default", formFields);

        Map<String, Object> data = new HashMap<>();
        data.put("DateReceived", "1988-14-04");

        HocsForm form1 = new HocsForm(schema1, data);

        return form1;
    }

    private static HocsForm getAllocationNote(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "AllocationNote");
        properties7.put("label", "Allocation Note");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Allocation Note", "Default", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;

    }

    private static HocsForm getTopics(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "Topics");
        properties7.put("label", "Topics");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Topics", "Default", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;

    }

}
