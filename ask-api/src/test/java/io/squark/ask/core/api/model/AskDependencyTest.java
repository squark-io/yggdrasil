package io.squark.ask.core.api.model;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-03-20.
 * Copyright 2016
 */
public class AskDependencyTest {

    private AskDependency askDependency;

    @BeforeMethod
    public void setupTest() {
        this.askDependency = new AskDependency();
        askDependency.setGroupId("groupId");
        askDependency.setArtifactId("artifactId");
        askDependency.setVersion("1.0");
        askDependency.setClassifier("jar");
        askDependency.setScope("provided");
    }

    @Test
    public void testAddChild() {
        AskDependency parent = new AskDependency();
        parent.addChildDependency(askDependency);
        Assert.assertTrue(parent.getChildDependencies().contains(askDependency));
    }

    @Test
    public void testCompare() {
        AskDependency compare = new AskDependency();
        compare.setGroupId("groupId");
        compare.setArtifactId("artifactId");
        compare.setVersion("1.0");
        compare.setClassifier("jar");
        compare.setScope("provided");
        Assert.assertEquals(compare, askDependency);
    }

}