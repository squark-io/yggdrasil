package org.dynamicjar.core.model;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-09.
 * Copyright 2016
 */
public class DependencyTreeNode {
    private DependencyTree parentTree;
    private String groupId;
    private String artifactId;
    private String extension;
    private String classifier;
    private String version;
    private File file;
    private String scope;
    private DependencyTree childDependencies;

    private DependencyTreeNode(String groupId, String artifactId, String extension, String version,
        File file) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.version = version;
        this.file = file;
        this.childDependencies = new DependencyTree(this);
    }

    private DependencyTreeNode(DependencyNode dependencyNode, @Nullable String defaultScope,
        @NotNull DependencyTree parentTree) {
        this(dependencyNode, defaultScope);
        this.parentTree = parentTree;

    }

    public DependencyTreeNode(DependencyNode dependencyNode, @Nullable String defaultScope) {
        Artifact artifact = dependencyNode.getArtifact();
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.extension = artifact.getExtension();
        this.classifier = artifact.getClassifier();
        this.version = artifact.getVersion();
        this.file = artifact.getFile();
        this.scope = dependencyNode.getDependency().getScope();
        if (StringUtils.isEmpty(this.scope)) {
            this.scope = defaultScope;
        }
        this.childDependencies = new DependencyTree(this);
        if (CollectionUtils.isNotEmpty(dependencyNode.getChildren())) {
            dependencyNode.getChildren().stream().forEach(childNode -> {
                //No default scope for child nodes.
                this.childDependencies
                    .put(new DependencyTreeNode(childNode, null, this.childDependencies));
            });
        }
    }

    public static DependencyTreeNode fromMavenProject(MavenProject mavenProject) {
        return new DependencyTreeNode(mavenProject.getGroupId(), mavenProject.getArtifactId(),
            mavenProject.getPackaging(), mavenProject.getVersion(), mavenProject.getFile());
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

    public boolean hasChildDependencies() {
        return childDependencies != null && childDependencies.size() > 0;
    }

    public DependencyTree getChildDependencies() {
        return childDependencies;
    }

    public void addChildDependency(DependencyTreeNode childDependency) {
        if (this.childDependencies == null) {
            this.childDependencies = new DependencyTree(this);
        }
        this.childDependencies.put(childDependency);
        childDependency.setParentTree(this.childDependencies);
    }

    public DependencyTree getParentTree() {
        return parentTree;
    }

    public void setParentTree(DependencyTree parentTree) {
        this.parentTree = parentTree;
    }

    public DependencyTreeNode getParent() {
        return this.parentTree != null ? this.parentTree.getDependencyTreeNode() : null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(groupId).append(artifactId).append(version)
            .append(classifier).append(extension).append(scope).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DependencyTreeNode that = (DependencyTreeNode) o;

        return new EqualsBuilder().append(groupId, that.groupId).append(artifactId, that.artifactId)
            .append(version, that.version).append(classifier, that.classifier)
            .append(extension, that.extension).append(scope, that.scope).isEquals();
    }

    @Override
    public String toString() {
        return "DependencyTreeNode{\n" +
               "\tparent='" + ((parentTree != null && parentTree.getDependencyTreeNode() != null) ?
                               parentTree.getDependencyTreeNode().buildIdentifierString() :
                               "null") + '\'' +
               ",\n\tgroupId='" + groupId + '\'' +
               ",\n\tartifactId='" + artifactId + '\'' +
               ",\n\textension='" + extension + '\'' +
               ",\n\tclassifier='" + classifier + '\'' +
               ",\n\tversion='" + version + '\'' +
               ",\n\tfile=" + file +
               ",\n\tscope='" + scope + '\'' +
               ",\n\tchildDependencies=" + childDependencies +
               "\n}";
    }

    public String buildIdentifierString() {
        return buildIdentifierString(this);
    }

    public static String buildIdentifierString(DependencyTreeNode dependencyTreeNode) {
        return buildIdentifierString(dependencyTreeNode.getGroupId(),
            dependencyTreeNode.getArtifactId(), dependencyTreeNode.getExtension(),
            dependencyTreeNode.getClassifier(), dependencyTreeNode.getVersion());
    }

    public static String buildIdentifierString(String groupId, String artifactId, String extension,
        @Nullable String classifier, String version) {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(groupId);
        buffer.append(':').append(artifactId);
        buffer.append(':').append(extension);
        if (StringUtils.isNotEmpty(classifier)) {
            buffer.append(':').append(classifier);
        }
        buffer.append(':').append(version);
        return buffer.toString();
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

    public String getClassifier() {
        return classifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
