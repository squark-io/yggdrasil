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
package io.squark.yggdrasil.core.main;

import io.squark.nestedjarclassloader.BootstrapClassLoader;
import io.squark.yggdrasil.core.api.util.LibHelper;
import io.squark.yggdrasil.core.api.util.ReflectionUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bootstrap.class, LibHelper.class, Yggdrasil.class, ReflectionUtil.class})
public class BootstrapIntegrationTest {

    @Test
    public void bootstrapTest() throws Exception {
        PowerMockito.spy(Bootstrap.class);
        URL ownJar = new URL("file", null, 0, new File("./target").getAbsolutePath());
        URL[] array = new URL[2];
        array[0] = Paths.get(new File("./target/classes/").getAbsoluteFile().toURI()).normalize().toUri().toURL();
        array[1] = Paths.get(new File("./target/test-classes/").getAbsoluteFile().toURI()).normalize().toUri().toURL();
        PowerMockito.mockStatic(LibHelper.class, invocationOnMock -> {
            if (invocationOnMock.getMethod().getName().equals("getLibs")) {
                return array;
            } else if (invocationOnMock.getMethod().getName().equals("getOwnJar")) {
                return ownJar;
            }
            return invocationOnMock.callRealMethod();
        });
        final boolean[] triggeredReflectionUtil = new boolean[1];
        PowerMockito.mockStatic(ReflectionUtil.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                triggeredReflectionUtil[0] = true;
                return null;
            }
        });
        BootstrapClassLoader classLoader = PowerMockito.spy(new BootstrapClassLoader(array));
        PowerMockito.whenNew(BootstrapClassLoader.class).withParameterTypes(URL.class).withArguments(ownJar)
            .thenAnswer(invocationOnMock -> classLoader);
        Bootstrap.main(new String[0]);
        Assert.assertTrue(triggeredReflectionUtil[0]);
    }

}