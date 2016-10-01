package io.squark.dynamicjar.core.api.model;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class DynamicJarDependency implements Serializable {

    private String groupId;
    private String artifactId;
    private String extension = "jar";
    private String classifier;
    private String version;
    private File file;
    private String scope;
    private Boolean excluded;
    private transient DynamicJarDependency parent;
    private Set<DynamicJarDependency> childDependencies;
    private Boolean optional;

    public DynamicJarDependency() {
    }

    public DynamicJarDependency(String groupId, String artifactId, String extension, String version, File file) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.version = version;
        this.file = file;
    }

    public DynamicJarDependency(String groupId, String artifactId, String extension, String classifier, String version, File file,
        String scope, @Nullable Set<DynamicJarDependency> childDependencies, @Nullable String defaultScope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.classifier = classifier;
        this.version = version;
        this.file = file;
        this.scope = scope;
        if (StringUtils.isEmpty(this.scope)) {
            this.scope = defaultScope;
        }
        this.childDependencies = new HashSet<>();
        if (CollectionUtils.isNotEmpty(childDependencies)) {
            for (DynamicJarDependency childDependency : childDependencies) {
                childDependency.setParent(this);
                this.childDependencies.add(childDependency);
            }
        }
    }

    public DynamicJarDependency(String groupId, String artifactId, String extension, String classifier, String version, File file,
        String scope, Set<DynamicJarDependency> children, String provided, Boolean optional) {
        this(groupId, artifactId, extension, classifier, version, file, scope, children, provided);
        this.optional = optional;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getExcluded() {
        return excluded;
    }

    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    public Optional<DynamicJarDependency> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(DynamicJarDependency parent) {
        this.parent = parent;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public Set<DynamicJarDependency> getChildDependencies() {
        return childDependencies;
    }

    public void setChildDependencies(Set<DynamicJarDependency> childDependencies) {
        this.childDependencies = childDependencies;
    }

    public void addChildDependency(DynamicJarDependency childDependency) {
        if (childDependencies == null) {
            childDependencies = new HashSet<>();
        }
        childDependencies.add(childDependency);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(groupId).append(artifactId).append(extension).append(classifier).append(version)
            .append(scope).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DynamicJarDependency that = (DynamicJarDependency) o;

        return new EqualsBuilder().append(groupId, that.groupId).append(artifactId, that.artifactId)
            .append(extension, that.extension).append(classifier, that.classifier).append(version, that.version)
            .append(scope, that.scope).isEquals();
    }

    @Override
    public String toString() {
        return "DynamicJarDependency{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId + '\'' + ", extension='" +
               extension + '\'' + ", classifier='" + classifier + '\'' + ", version='" + version + '\'' + ", file=" + file +
               ", scope='" + scope + '\'' + ", optional'" + optional + '\'' + ", parent='" +
               (getParent().isPresent() ? getParent().get().toShortString() : null) + ", childDependencies=" + childDependencies +
               '}';
    }

    public String toShortString() {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(groupId);
        buffer.append(':').append(artifactId);
        buffer.append(':').append(extension);
        if (StringUtils.isNotEmpty(classifier)) {
            buffer.append(':').append(classifier);
        }
        if (StringUtils.isNotEmpty(version)) {
            buffer.append(':').append(version);
        }
        return buffer.toString();
    }

    public String toShortStringWithoutVersion() {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(groupId);
        buffer.append(':').append(artifactId);
        buffer.append(':').append(extension);
        if (StringUtils.isNotEmpty(classifier)) {
            buffer.append(':').append(classifier);
        }
        return buffer.toString();
    }
}
