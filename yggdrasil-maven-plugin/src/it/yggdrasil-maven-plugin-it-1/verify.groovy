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
import java.util.jar.JarEntry
import java.util.jar.JarFile

File configFile = new File(basedir, "target/classes/META-INF/yggdrasil.json");
assert configFile.isFile()
assert configFile.text.contains("\"mainClass\": \"mockclass\"");

JarFile jarFile = new JarFile(new File(basedir, "target/yggdrasil-maven-plugin-it-1-${projectVersion}-yggdrasil.jar"))

JarEntry configEntry = jarFile.getEntry("META-INF/yggdrasil.json") as JarEntry;
assert configEntry != null;

JarEntry classEntry = jarFile.getEntry("io/squark/yggdrasil/core/main/Bootstrap.class") as JarEntry
assert classEntry != null;
