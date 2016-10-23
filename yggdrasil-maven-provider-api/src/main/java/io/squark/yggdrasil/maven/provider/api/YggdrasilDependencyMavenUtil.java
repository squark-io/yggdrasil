/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.maven.provider.api;

import io.squark.yggdrasil.core.api.model.YggdrasilDependency;
import io.squark.yggdrasil.core.api.util.Scopes;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface YggdrasilDependencyMavenUtil {

    static YggdrasilDependency fromDependencyNode(final DependencyNode dependencyNode,
        List<String> exclusions) {
        Artifact artifact = dependencyNode.getArtifact();
        String groupId = artifact != null ? artifact.getGroupId() : null;
        String artifactId = artifact != null ? artifact.getArtifactId() : null;
        String extension = artifact != null ? artifact.getExtension() : null;
        String classifier = artifact != null ? artifact.getClassifier() : null;
        String version = artifact != null ? artifact.getVersion() : null;
        File file = artifact != null ? artifact.getFile() : null;
        Dependency aetherDependency = dependencyNode.getDependency();
        Boolean optional = aetherDependency != null ? aetherDependency.getOptional() : null;
        String scope = aetherDependency != null ? aetherDependency.getScope() : null;
        Set<YggdrasilDependency> children = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dependencyNode.getChildren())) {
            for (DependencyNode child : dependencyNode.getChildren()) {
                children.add(fromDependencyNode(child, exclusions));
            }
        }
        YggdrasilDependency dependency =
            new YggdrasilDependency(groupId, artifactId, extension, classifier, version, file, scope, children,
                Scopes.PROVIDED, optional);
        if (exclusions != null) {
            for (String exclusion : exclusions) {
                Pattern pattern = Pattern.compile(exclusion);
                Matcher matcher = pattern.matcher(dependency.toShortStringWithoutVersion());
                if (matcher.matches()) {
                    dependency.setExcluded(true);
                }
            }
        }
        return dependency;
    }
}
