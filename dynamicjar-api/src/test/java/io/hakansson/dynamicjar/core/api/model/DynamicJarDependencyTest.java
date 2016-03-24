package io.hakansson.dynamicjar.core.api.model;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-20.
 * Copyright 2016
 */
public class DynamicJarDependencyTest {

    private DynamicJarDependency dynamicJarDependency;

    @BeforeMethod
    public void setupTest() {
        this.dynamicJarDependency = new DynamicJarDependency();
        dynamicJarDependency.setGroupId("groupId");
        dynamicJarDependency.setArtifactId("artifactId");
        dynamicJarDependency.setVersion("1.0");
        dynamicJarDependency.setClassifier("jar");
        dynamicJarDependency.setScope("provided");
    }

    @Test
    public void testAddChild() {
        DynamicJarDependency parent = new DynamicJarDependency();
        parent.addChildDependency(dynamicJarDependency);
        Assert.assertTrue(parent.getChildDependencies().contains(dynamicJarDependency));
    }

    @Test
    public void testCompare() {
        DynamicJarDependency compare = new DynamicJarDependency();
        compare.setGroupId("groupId");
        compare.setArtifactId("artifactId");
        compare.setVersion("1.0");
        compare.setClassifier("jar");
        compare.setScope("otherScope"); //Scope should not be compared
        Assert.assertEquals(compare, dynamicJarDependency);
    }

}