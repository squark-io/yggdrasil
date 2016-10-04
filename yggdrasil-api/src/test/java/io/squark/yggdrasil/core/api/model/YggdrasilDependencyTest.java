package io.squark.yggdrasil.core.api.model;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-03-20.
 * Copyright 2016
 */
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