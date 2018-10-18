package uk.gov.digital.ho.hocs.workflow;


import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsFormAction;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HocsFormService {

    private Map<String, HocsForm> forms = new HashMap<>();

    public HocsFormService() {

        // DCU

        //// Create
        forms.put("DCU_CORRESPONDENCE_DETAILS", getDataInput());
        forms.put("DCU_CORRESPONDENCE_DETAILS_DTEN", getDataInputDTEN());
        forms.put("DCU_CORRESPONDENT_LOOKUP", getCorrespondentLookup());
        forms.put("DCU_ADD_CORRESPONDENT", getAddCorrespondentPublic());
        forms.put("DCU_ADD_ANOTHER_CORRESPONDENT", getAddAnotherCorrespondent());
        forms.put("DCU_SET_PRIMARY_CORRESPONDENT", getSetPrimaryCorrespondent());

        //// Draft
        forms.put("DCU_INITIAL_DRAFT_DECISION", getInitialDraftConfirmation());
        forms.put("DCU_REJECTION_NOTE", getRejectionNote());
        forms.put("DCU_RESPONSE_CHANNEL", getResponseChannel());
        forms.put("DCU_PHONECALL_NOTE", getPhonecallNote());
        forms.put("DCU_UPLOAD_DOCUMENT", getUploadDraftDocument());
        forms.put("DCU_OFFLINE_QA_DECISION", getOfflineQADecision());
        forms.put("DCU_OFFLINE_QA_DETAILS", getOfflineQADetails());
        forms.put("APPROVE_QA_RESPONSE", getApproveQAResponse());

        //// MARKUP
        forms.put("MARKUP_DECISION", getMarkupDecision());
        forms.put("TOPICS", getTopics());

        //// Private Office
        forms.put("APPROVE_PRIVATE_OFFICE", getApprovePrivateOffice());
        forms.put("APPROVE_PRIVATE_OFFICE_DTEN", getApprovePrivateOfficeDTEN());

        ////Minister
        forms.put("APPROVE_MINISTER_SIGN_OFF", getApproveMinisterSignOff());
        forms.put("CHANGE_MINISTER", getChangeMinister());
        forms.put("CHANGE_MINISTER_NOTE", getChangeMinisterNote());

        ////Dispatch
        forms.put("APPROVE_DISPATCH", getApproveDispatch());

        //// Copy Number Ten
        forms.put("COPY_NUMBER_TEN_RESPONSE", getCopyNumberTen());

        //// Transfer Confirmation
        forms.put("TRANSFER_CONFIRMATION", getTransferConfirmation());

        //// No Reply Needed Confirmation
        forms.put("NO_REPLY_NEEDED_CONFIRMATION", getNoReplyNeededConfirmation());

        // UKVI
        forms.put("INITIAL_DECISION_UKVI", getInitialDecisionUKVI());
        forms.put("OWNING_MEMBER", getOwningMember());
        forms.put("ANSWERING", getAllocateForm());

        // Shared
        forms.put("DEADLINES", getDeadlinesForm());
        forms.put("ALLOCATION_NOTE", getAllocationNote(false, "Allocation", "Allocation Note"));
    }

    public HocsForm getForm(String form) {
        return forms.get(form);
    }

    private HocsForm getDataInput() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties1 = new HashMap<>();
        properties1.put("name", "DateReceived");
        properties1.put("label", "When was the correspondence received?");

        HocsFormField fieldOne = new HocsFormField("date", validationList, properties1);

        Map<String, Object> properties2 = new HashMap<>();
        properties2.put("name", "DateOfCorrespondence");
        properties2.put("label", "When was the correspondence sent?");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Email");
        choice1.put("value", "EMAIL");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Post");
        choice2.put("value", "POST");

        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "Phone");
        choice3.put("value", "PHONE");

        Map<String, String> choice4 = new HashMap<>();
        choice4.put("label", "No. 10");
        choice4.put("value", "NO10");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);
        choices.add(choice4);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "OriginalChannel");
        properties4.put("label", "How was the correspondence received?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String, String> choice7 = new HashMap<>();
        choice7.put("label", "Send a copy to Number 10?");
        choice7.put("value", "TRUE");

        List<Map<String, String>> choices7 = new ArrayList<>();
        choices7.add(choice7);

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "CopyNumberTen");
        properties7.put("label", "");
        properties7.put("choices", choices7);

        HocsFormField fieldSeven = new HocsFormField("checkbox", new ArrayList<>(), properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldTwo);
        formFields.add(fieldOne);
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }
    private HocsForm getDataInputDTEN() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties1 = new HashMap<>();
        properties1.put("name", "DateReceived");
        properties1.put("label", "When was the correspondence received?");

        HocsFormField fieldOne = new HocsFormField("date", validationList, properties1);

        Map<String, Object> properties2 = new HashMap<>();
        properties2.put("name", "DateOfCorrespondence");
        properties2.put("label", "When was the correspondence sent?");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Email");
        choice1.put("value", "EMAIL");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Post");
        choice2.put("value", "POST");

        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "Phone");
        choice3.put("value", "PHONE");

        Map<String, String> choice4 = new HashMap<>();
        choice4.put("label", "No. 10");
        choice4.put("value", "NO10");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);
        choices.add(choice4);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "OriginalChannel");
        properties4.put("label", "How was the correspondence received?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<Map<String, String>> choices7 = new ArrayList<>();

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldTwo);
        formFields.add(fieldOne);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getCorrespondentLookup() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties10 = new HashMap<>();
        List<String> help = new ArrayList<>();
        help.add("Type or Select the Correspondent's full name as it should appear in the response.");
        properties10.put("children", help);

        HocsFormField fieldTen = new HocsFormField("inset", new ArrayList<>(), properties10);


        Map<String, Object> properties3 = new HashMap<>();
        properties3.put("name", "TEMPCFullName");
        properties3.put("label", "Full Name");

        HocsFormField fieldThree = new HocsFormField("text", validationList, properties3);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldTen);
        formFields.add(fieldThree);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Correspondent Full Name", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getAddCorrespondentPublic() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Correspondent");
        choice1.put("value", "CORRESPONDENT");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Other");
        choice2.put("value", "OTHER");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> propertiesA = new HashMap<>();
        propertiesA.put("name", "TEMPCType");
        propertiesA.put("label", "What is the correspondent type?");
        propertiesA.put("choices", choices);

        HocsFormField fieldABCD = new HocsFormField("dropdown", validationList, propertiesA);

        Map<String, Object> properties1 = new HashMap<>();
        properties1.put("name", "TEMPCFullName");
        properties1.put("label", "Full Name");

        HocsFormField fieldOne = new HocsFormField("text", new ArrayList<>(), properties1);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "TEMPCPostcode");
        properties4.put("label", "Postcode");

        HocsFormField fieldFour = new HocsFormField("text", validationList, properties4);

        Map<String, Object> properties5 = new HashMap<>();
        properties5.put("name", "TEMPCAddressOne");
        properties5.put("label", "Building");

        HocsFormField fieldFive = new HocsFormField("text", validationList, properties5);

        Map<String, Object> properties9 = new HashMap<>();
        properties9.put("name", "TEMPCAddressTwo");
        properties9.put("label", "Street");

        HocsFormField fieldNine = new HocsFormField("text", new ArrayList<>(), properties9);

        Map<String, Object> properties6 = new HashMap<>();
        properties6.put("name", "TEMPCEmail");
        properties6.put("label", "Email Address");

        HocsFormField fieldSix = new HocsFormField("text", new ArrayList<>(), properties6);

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "TEMPCPhone");
        properties7.put("label", "Telephone");

        HocsFormField fieldSeven = new HocsFormField("text", new ArrayList<>(), properties7);

        Map<String, Object> properties8 = new HashMap<>();
        properties8.put("name", "TEMPCAddressThree");
        properties8.put("label", "Town or City");

        HocsFormField fieldEight = new HocsFormField("text", new ArrayList<>(), properties8);

        Map<String, Object> properties11 = new HashMap<>();
        properties11.put("name", "TEMPCReference");
        properties11.put("label", "Does this correspondent give a case reference?");

        HocsFormField fieldEleven = new HocsFormField("text", new ArrayList<>(), properties11);


        Map<String, Object> properties10 = new HashMap<>();
        List<String> help = new ArrayList<>();
        help.add("Add as many details as you can for the correspondent. You will be able to add additional correspondents after this.");
        properties10.put("children", help);

        HocsFormField fieldTen = new HocsFormField("inset", new ArrayList<>(), properties10);


        Map<String, String> choice10 = new HashMap<>();
        choice10.put("label", "United Kingdom");
        choice10.put("value", "12345");

        Map<String, String> choice20 = new HashMap<>();
        choice20.put("label", "Other");
        choice20.put("value", "56789");

        List<Map<String, String>> choices1 = new ArrayList<>();
        choices1.add(choice10);
        choices1.add(choice20);

        Map<String, Object> propertiesB = new HashMap<>();
        propertiesB.put("name", "TEMPCCountry");
        propertiesB.put("label", "Country");
        propertiesB.put("choices", choices1);

        HocsFormField fieldCDEF = new HocsFormField("dropdown", validationList, propertiesB);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldTen);
        formFields.add(fieldABCD);
        formFields.add(fieldOne);
        formFields.add(fieldFive);
        formFields.add(fieldNine);
        formFields.add(fieldEight);
        formFields.add(fieldFour);
        formFields.add(fieldCDEF);
        formFields.add(fieldSeven);
        formFields.add(fieldSix);
        formFields.add(fieldEleven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondent Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getAddAnotherCorrespondent() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Yes");
        choice1.put("value", "TRUE");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "No");
        choice2.put("value", "FALSE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "TEMPAdditionalCorrespondent");
        properties4.put("label", "Do you need to add another correspondent?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);


        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldFour);
        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondent Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;

    }


    private HocsForm getSetPrimaryCorrespondent() {

        Map<String, Object> properties9 = new HashMap<>();
        properties9.put("name", "Correspondents");
        properties9.put("label", "Which is the primary correspondent?");
        properties9.put("action", "CORRESPONDENT");
        properties9.put("entity", "correspondent");
        properties9.put("hasRemoveLink", true);
        properties9.put("hasAddLink", true);
        properties9.put("choices", "CASE_CORRESPONDENTS");

        HocsFormField fieldNine = new HocsFormField("entity-list", new ArrayList<>(), properties9);

        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldNine);
        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getTransferConfirmation() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Refer To OGD");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject Transfer");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "TransferConfirmation");
        properties4.put("label", "Transfer Confirmation");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Transfer Confirmation", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getNoReplyNeededConfirmation() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "NoReplyNeededConfirmation");
        properties4.put("label", "No Reply Needed Confirmation");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "No Reply Needed Confirmation", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getInitialDraftConfirmation() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Yes");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "No");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "InitialDraftDecision");
        properties4.put("label", "Should this correspondence be answered by your team?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String, Object> properties5 = new HashMap<>();
        properties5.put("children", "If you select 'No' you will be asked to explain why and the case will return to DCU.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getMarkupDecision() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Policy Response");
        choice1.put("value", "PR");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "FAQ");
        choice2.put("value", "FAQ");

        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "Refer To OGD");
        choice3.put("value", "OGD");

        Map<String, String> choice4 = new HashMap<>();
        choice4.put("label", "No Reply Needed");
        choice4.put("value", "NRN");
        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);
        choices.add(choice4);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "MarkupDecision");
        properties4.put("label", "Markup Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Markup Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getAllocateForm() {
        List<String> validationList = new ArrayList<>();
        //alidationList.add("required");

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "MarkupUnit");
        properties4.put("label", "Markup Unit");

        HocsFormField fieldFour = new HocsFormField("dropdown", validationList, properties4);

        Map<String, Object> properties5 = new HashMap<>();
        properties5.put("name", "MarkupTeam");
        properties5.put("label", "Markup Team");

        HocsFormField fieldFive = new HocsFormField("dropdown", validationList, properties5);

        Map<String, Object> properties6 = new HashMap<>();
        properties6.put("name", "SignOffMinister");
        properties6.put("label", "Sign-Off Minister");
        properties6.put("choices", "MINISTERS");

        HocsFormField fieldSix = new HocsFormField("dropdown", validationList, properties6);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);
        formFields.add(fieldSix);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Answering", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getChangeMinister() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties6 = new HashMap<>();
        properties6.put("name", "SignOffMinister");
        properties6.put("label", "Sign-Off Minister");
        properties6.put("choices", "MINISTERS");

        HocsFormField fieldSix = new HocsFormField("dropdown", validationList, properties6);

        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldSix);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Change Minister", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getOwningMember() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "owningMember");
        properties.put("label", "Owning Member");

        HocsFormField dropDown = new HocsFormField("dropdown", validationList, properties);

        List<HocsFormField> fields = new ArrayList<>();
        fields.add(dropDown);

        HocsSchema schema = new HocsSchema(HocsFormAction.SUBMIT, "Owning Member", "Continue", fields);

        HocsForm form1 = new HocsForm(schema);
        return form1;
    }

    private HocsForm getDeadlinesForm() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String, Object> properties2 = new HashMap<>();
        properties2.put("name", "DraftingDeadline");
        properties2.put("label", "Drafting Deadline");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String, Object> properties3 = new HashMap<>();
        properties3.put("name", "FinalDeadline");
        properties3.put("label", "Final Deadline");

        HocsFormField fieldThree = new HocsFormField("date", validationList, properties3);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldTwo);
        formFields.add(fieldThree);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Deadlines", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getPhonecallNote() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "PhonecallNote");
        properties7.put("label", "Please summarise your call.");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        Map<String, Object> properties5 = new HashMap<>();
        properties5.put("children", "You can return to this page after your call.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFive);
        formFields.add(fieldSeven);


        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;

    }

    private HocsForm getChangeMinisterNote() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "CaseNote_ChangeMinisterNote");
        properties7.put("label", "Why should this be answered by another minister?");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Decision", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;

    }

    private HocsForm getRejectionNote() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "CaseNote_RejectionNote");
        properties7.put("label", "Why should this should not be answered by your team?");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Decision", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;

    }

    private HocsForm getAllocationNote(boolean required, String title, String question) {
        List<String> validationList = new ArrayList<>();
        if (required) {
            validationList.add("required");
        }

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "CaseNote_" + title + "Note");
        properties7.put("label", question);

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, title + " Note", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;

    }

    private HocsForm getTopics() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties7 = new HashMap<>();
        properties7.put("name", "Topics");
        properties7.put("label", "Which is the primary Topic?");
        properties7.put("action", "TOPIC");
        properties7.put("entity", "topic");
        properties7.put("hasRemoveLink", true);
        properties7.put("hasAddLink", true);
        properties7.put("choices", "CASE_TOPICS");

        HocsFormField fieldSeven = new HocsFormField("entity-list", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Topics", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;

    }

    private HocsForm getInitialDecisionUKVI() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Send To Draft");
        choice1.put("value", "DRAFT");
        choice1.put("checked", "checked");

        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "Refer To OGD");
        choice3.put("value", "OGD");

        Map<String, String> choice4 = new HashMap<>();
        choice4.put("label", "No Reply Needed");
        choice4.put("value", "NRN");
        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice3);
        choices.add(choice4);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "InitialDecision");
        properties4.put("label", "Initial Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Initial Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }


    private HocsForm getResponseChannel() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "Letter");
        choice3.put("value", "LETTER");

        Map<String, String> choice4 = new HashMap<>();
        choice4.put("label", "Phone");
        choice4.put("value", "PHONE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice3);
        choices.add(choice4);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "ResponseChannel");
        properties4.put("label", "How do you intend to respond?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String, Object> properties5 = new HashMap<>();
        properties5.put("children", "If you select 'Phone' you will be asked to summarise the call and then the case will close.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getUploadDraftDocument() {

        Map<String, Object> properties9 = new HashMap<>();
        properties9.put("name", "Documents");
        properties9.put("label", "Which is the primary draft document?");
        properties9.put("entity", "document");
        properties9.put("hasRemoveLink", true);
        properties9.put("hasAddLink", true);
        properties9.put("choices", "CASE_DOCUMENT_LIST_DRAFT");

        HocsFormField fieldNine = new HocsFormField("entity-list", new ArrayList<>(), properties9);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldNine);


        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Upload Draft", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getOfflineQADecision() {

        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "No");
        choice3.put("value", "FALSE");

        Map<String, String> choice4 = new HashMap<>();
        choice4.put("label", "Yes");
        choice4.put("value", "TRUE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice3);
        choices.add(choice4);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "OfflineQA");
        properties4.put("label", "Do you want to QA this offline?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);


        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Offline QA Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getOfflineQADetails() {

        List<HocsFormField> formFields = new ArrayList<>();

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Offline QA Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getApproveQAResponse() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "QAResponseDecision");
        properties4.put("label", "QA Response");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String, Object> properties5 = new HashMap<>();
        properties5.put("children", "If you select 'No' you will be asked to explain why and the case will return to Initial Draft.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "QA Response", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getApprovePrivateOffice() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        Map<String, String> choice3 = new HashMap<>();
        choice3.put("label", "Change Minister");
        choice3.put("value", "CHANGE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "PrivateOfficeDecision");
        properties4.put("label", "Private Office Sign Off");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Private Office Sign Off", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getApprovePrivateOfficeDTEN() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "PrivateOfficeDecision");
        properties4.put("label", "Private Office Sign Off");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Private Office Sign Off", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getApproveMinisterSignOff() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "MinisterSignOffDecision");
        properties4.put("label", "Minister Sign Off");
        properties4.put("choices", choices);
        
        Map<String, Object> signOffMinisterProps = new HashMap<>();
        signOffMinisterProps.put("name", "SignOffMinister");
        signOffMinisterProps.put("children", "Sign-off Minister: ");

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);
        HocsFormField signOffMinister = new HocsFormField("inset", new ArrayList<>(), signOffMinisterProps);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(signOffMinister);
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Minister Sign Off", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getApproveDispatch() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String, String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String, Object> properties4 = new HashMap<>();
        properties4.put("name", "DispatchDecision");
        properties4.put("label", "Dispatch");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Dispatch", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }

    private HocsForm getCopyNumberTen() {

        List<HocsFormField> formFields = new ArrayList<>();

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Dispatched", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1);

        return form1;
    }
}
