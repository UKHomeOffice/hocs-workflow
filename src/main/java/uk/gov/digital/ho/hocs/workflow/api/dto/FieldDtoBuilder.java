package uk.gov.digital.ho.hocs.workflow.api.dto;

import java.util.Map;
import java.util.UUID;

@Deprecated(forRemoval = true)
public final class FieldDtoBuilder {

    private UUID uuid;

    private String name;

    private String label;

    private String component;

    private Object[] validation;

    private Map<String, Object> props;

    private boolean active;

    private FieldDto child;

    private FieldDtoBuilder() {
    }

    public static FieldDtoBuilder aFieldDto() {
        return new FieldDtoBuilder();
    }

    public FieldDtoBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public FieldDtoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public FieldDtoBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    public FieldDtoBuilder withComponent(String component) {
        this.component = component;
        return this;
    }

    public FieldDtoBuilder withValidation(Object[] validation) {
        this.validation = validation;
        return this;
    }

    public FieldDtoBuilder withProps(Map<String, Object> props) {
        this.props = props;
        return this;
    }

    public FieldDtoBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public FieldDtoBuilder withChild(FieldDto child) {
        this.child = child;
        return this;
    }

    public FieldDto build() {
        return new FieldDto(uuid, name, label, component, validation, props, active, child);
    }

}
