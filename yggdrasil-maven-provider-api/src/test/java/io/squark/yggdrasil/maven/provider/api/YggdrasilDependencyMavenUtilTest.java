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
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

public class YggdrasilDependencyMavenUtilTest {

    @Test
    public void fromDependencyNodeNullTest() {
        DependencyNode nullDependencyNode = new DefaultDependencyNode((Dependency) null);
        YggdrasilDependency nullDependency = YggdrasilDependencyMavenUtil.fromDependencyNode(nullDependencyNode, null);
        Assertions.assertAll(() -> Assertions.assertNull(nullDependency.getGroupId()),
            () -> Assertions.assertNull(nullDependency.getArtifactId()), () -> Assertions.assertNull(nullDependency.getVersion()),
            () -> Assertions.assertNull(nullDependency.getClassifier()),
            () -> Assertions.assertNull(nullDependency.getExtension()), () -> Assertions.assertNull(nullDependency.getOptional()),
            () -> Assertions.assertNull(nullDependency.getFile()));
    }

    @Test
    public void fromDependencyNodeTest() {
        Artifact artifact = new DefaultArtifact("test", "test", "default", "jar", "1.0", null, new File(""));
        Dependency dependency = new Dependency(artifact, null, false);
        DependencyNode dependencyNode = new DefaultDependencyNode(dependency);
        Artifact child = new DefaultArtifact("child", "child", null, null);
        DependencyNode childNode = new DefaultDependencyNode(child);
        dependencyNode.setChildren(Collections.singletonList(childNode));
        YggdrasilDependency yggdrasilDependency =
            YggdrasilDependencyMavenUtil.fromDependencyNode(dependencyNode, Collections.singletonList("test:test:jar:default"));
        Assertions.assertAll(
            () -> Assertions.assertEquals("test", yggdrasilDependency.getGroupId()),
            () -> Assertions.assertEquals("test", yggdrasilDependency.getArtifactId()),
            () -> Assertions.assertEquals("1.0", yggdrasilDependency.getVersion()),
            () -> Assertions.assertEquals("default", yggdrasilDependency.getClassifier()),
            () -> Assertions.assertEquals("jar", yggdrasilDependency.getExtension()),
            () -> Assertions.assertEquals("", yggdrasilDependency.getFile().getPath()),
            () -> Assertions.assertFalse(yggdrasilDependency.getOptional()),
            () -> Assertions.assertTrue(yggdrasilDependency.getExcluded())
        );
    }

}