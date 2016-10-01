package io.squark.ask.frameworkprovider.jpa;

import org.junit.Test;

import java.net.URL;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-06-19.
 * Copyright 2016
 */
public class CustomArchiveDescriptorFactoryTest {
    @Test(expected = IllegalArgumentException.class)
    public void buildArchiveDescriptorFileDoesNotExist() throws Exception {
        CustomArchiveDescriptorFactory customArchiveDescriptorFactory = new CustomArchiveDescriptorFactory();
        customArchiveDescriptorFactory.buildArchiveDescriptor(new URL("file://test"), "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildArchiveDescriptorJar() throws Exception {
        CustomArchiveDescriptorFactory customArchiveDescriptorFactory = new CustomArchiveDescriptorFactory();
        customArchiveDescriptorFactory.buildArchiveDescriptor(new URL("file://test!/test!/test"), "");
    }

}