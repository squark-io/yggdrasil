/*
 * Copyright (c) 2017 Erik Håkansson, http://squark.io
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
 * Copyright (c) 2017 Erik Håkansson, http://squark.io
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
package io.squark.yggdrasil.core.api;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.List;

public class YggdrasilServletInfo {

  private final String name;
  private final Class<? extends Servlet> clazz;
  private List<String> mappings = new ArrayList<>();
  private int loadOnStartup;
  private boolean asyncSupported;
  private boolean requireWelcomeFileMapping;

  private YggdrasilServletInfo(String name, Class<? extends Servlet> clazz) {
    this.name = name;
    this.clazz = clazz;
  }

  public static YggdrasilServletInfo servlet(String name,
    Class<? extends Servlet> clazz) {
    return new YggdrasilServletInfo(name, clazz);
  }

  public YggdrasilServletInfo addMapping(String mapping) {
    this.mappings.add(mapping);
    return this;
  }

  public YggdrasilServletInfo setLoadOnStartup(int loadOnStartup) {
    this.loadOnStartup = loadOnStartup;
    return this;
  }

  public int getLoadOnStartup() {
    return loadOnStartup;
  }

  public YggdrasilServletInfo setAsyncSupported(boolean asyncSupported) {
    this.asyncSupported = asyncSupported;
    return this;
  }

  public boolean isAsyncSupported() {
    return asyncSupported;
  }

  public YggdrasilServletInfo setRequireWelcomeFileMapping(boolean requireWelcomeFileMapping) {
    this.requireWelcomeFileMapping = requireWelcomeFileMapping;
    return this;
  }

  public String getName() {
    return name;
  }

  public Class<? extends Servlet> getClazz() {
    return clazz;
  }

  public boolean getAsyncSupported() {
    return asyncSupported;
  }

  public boolean getRequireWelcomeFileMapping() {
    return requireWelcomeFileMapping;
  }

  public List<String> getMappings() {
    return mappings;
  }
}
