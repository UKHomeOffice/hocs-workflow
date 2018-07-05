package uk.gov.digital.ho.hocs.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AddDocumentRequest {

    private DocumentSummary documentSummary;
}
