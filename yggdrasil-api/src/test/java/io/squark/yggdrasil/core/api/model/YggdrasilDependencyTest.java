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
package io.squark.yggdrasil.core.api.model;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class YggdrasilDependencyTest {

    private YggdrasilDependency yggdrasilDependency;

    @BeforeMethod
    public void setupTest() {
        this.yggdrasilDependency = new YggdrasilDependency();
        yggdrasilDependency.setGroupId("groupId");
        yggdrasilDependency.setArtifactId("artifactId");
        yggdrasilDependency.setVersion("1.0");
        yggdrasilDependency.setClassifier("jar");
        yggdrasilDependency.setScope("provided");
    }

    @Test
    public void testAddChild() {
        YggdrasilDependency parent = new YggdrasilDependency();
        parent.addChildDependency(yggdrasilDependency);
        Assert.assertTrue(parent.getChildDependencies().contains(yggdrasilDependency));
    }

    @Test
    public void testCompare() {
        YggdrasilDependency compare = new YggdrasilDependency();
        compare.setGroupId("groupId");
        compare.setArtifactId("artifactId");
        compare.setVersion("1.0");
        compare.setClassifier("jar");
        compare.setScope("provided");
        Assert.assertEquals(compare, yggdrasilDependency);
    }

}