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
package io.squark.yggdrasil.core.api.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-10-30.
 * Copyright 2016
 */
public class LibHelperTest {
    @Test
    public void getOwnJar() throws Exception {
        Assert.assertNotNull(LibHelper.getOwnJar());
    }

    @Test
    public void getLibs() throws Exception {
        URL[] libs = LibHelper.getLibs(LibHelperTest.class, "");
        Assert.assertNotNull(libs);
        Assert.assertEquals(2, libs.length);
        Assert.assertTrue("dummy.jar", libs[0].getFile().endsWith("dummy.jar"));
        Assert.assertTrue("dummy.jar", libs[1].getFile().endsWith("dummy.jar"));

        URL[] libs2 = LibHelper.getLibs(LibHelperTest.class, LibHelperTest.class.getName().replace('.', File.separatorChar) + ".class");
        Assert.assertNotNull(libs2);
        Assert.assertEquals(1, libs2.length);
        Assert.assertTrue(libs2[0].getFile().endsWith(LibHelperTest.class.getSimpleName() + ".class"));

    }

}