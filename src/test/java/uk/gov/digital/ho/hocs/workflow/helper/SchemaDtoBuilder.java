package uk.gov.digital.ho.hocs.workflow.helper;

import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.SecondaryActionDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SchemaDtoBuilder {

    private UUID uuid;

    private String stageType;

    private String type;

    private String title;

    private String defaultActionLabel;

    private boolean active;

    private List<FieldDto> fields;

    private List<SecondaryActionDto> secondaryActions;

    private Object props;

    private Object validation;

    private List<Object> summary;

    public SchemaDtoBuilder() {
        this.summary = new ArrayList<>();
    }

    public SchemaDtoBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public SchemaDtoBuilder withStageType(String stageType) {
        this.stageType = stageType;
        return this;
    }

    public SchemaDtoBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public SchemaDtoBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public SchemaDtoBuilder withDefaultActionLabel(String defaultActionLabel) {
        this.defaultActionLabel = defaultActionLabel;
        return this;
    }

    public SchemaDtoBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public SchemaDtoBuilder withFields(List<FieldDto> fields) {
        this.fields = fields;
        return this;
    }

    public SchemaDtoBuilder withSecondaryActions(List<SecondaryActionDto> secondaryActions) {
        this.secondaryActions = secondaryActions;
        return this;
    }

    public SchemaDtoBuilder withSecondaryActions(Object props) {
        this.props = props;
        return this;
    }

    public SchemaDtoBuilder withValidation(Object validation) {
        this.validation = validation;
        return this;
    }

    public SchemaDtoBuilder withSummary(Object summary) {
        this.summary.add(summary);
        return this;
    }

    public SchemaDto build() {
        return new SchemaDto(uuid, stageType, type, title, defaultActionLabel, active, fields, secondaryActions, props,
            validation, summary);
    }

}
