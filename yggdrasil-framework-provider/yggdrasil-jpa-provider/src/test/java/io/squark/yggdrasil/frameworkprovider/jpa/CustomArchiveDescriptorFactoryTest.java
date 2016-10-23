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
package io.squark.yggdrasil.frameworkprovider.jpa;

import org.junit.Test;

import java.net.URL;

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