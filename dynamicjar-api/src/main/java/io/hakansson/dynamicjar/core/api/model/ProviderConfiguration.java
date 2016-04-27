package io.hakansson.dynamicjar.core.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-27.
 * Copyright 2016
 */
public class ProviderConfiguration implements Serializable {

    private String identifier;
    private Map<String, Object> properties;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProviderConfiguration that = (ProviderConfiguration) o;

        return new EqualsBuilder().append(identifier, that.identifier)
            .append(properties, that.properties).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(identifier).append(properties).toHashCode();
    }
}
