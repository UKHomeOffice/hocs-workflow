package uk.gov.digital.ho.hocs.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CASE_CREATE_FAILURE;

@AllArgsConstructor
@Entity
@Table(name = "case_data")
public class CaseData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    @Column(name = "created")
    private LocalDate dateReceived = LocalDate.now();

    @Getter
    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();
    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "reference")
    private String reference;
    
    @Getter
    @Column(name = "uuid", columnDefinition ="uuid")
    private UUID uuid;

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, LocalDate dateReceived) {

        if (type == null || caseNumber == null) {
            throw new ApplicationExceptions.EntityCreationException("Cannot create CaseData", CASE_CREATE_FAILURE);
        }
        this.reference = generateCaseReference(this.type, caseNumber, this.created);
        this.type = type.getDisplayCode();
        this.dateReceived = dateReceived;
    }

    public CaseData() {
        //Empty constructor
    }

    public static String generateCaseReference(String type, Long caseNumber, LocalDateTime createdDateTime) {
        return String.format("%S/%07d/%ty", type, caseNumber, createdDateTime);
    }
}
