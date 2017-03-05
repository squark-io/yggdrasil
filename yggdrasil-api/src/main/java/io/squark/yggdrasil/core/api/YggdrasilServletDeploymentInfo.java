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

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YggdrasilServletDeploymentInfo {
  private Map<String, Object> servletContextAttributes = new HashMap<>();
  private List<Class<? extends EventListener>> listeners = new ArrayList<>();
  private ClassLoader classLoader;
  private String contextPath;
  private String deploymentName;
  private Map<String, String> initParameters = new HashMap<>();
  private boolean eagerFilterInit;
  private List<YggdrasilServletInfo> servlets = new ArrayList<>();
  private List<String> welcomePages = new ArrayList<>();
  private Map<String, Object> contextObjects = new HashMap<>();

  public YggdrasilServletDeploymentInfo addListener(Class<? extends EventListener> listenerClass) {
    this.listeners.add(listenerClass);
    return this;
  }

  public YggdrasilServletDeploymentInfo addServletContextAttribute(String name, Object deployment) {
    this.servletContextAttributes.put(name, deployment);
    return this;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public YggdrasilServletDeploymentInfo setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  public String getContextPath() {
    return contextPath;
  }

  public YggdrasilServletDeploymentInfo setContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  public String getDeploymentName() {
    return deploymentName;
  }

  public YggdrasilServletDeploymentInfo setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
    return this;
  }

  public YggdrasilServletDeploymentInfo addInitParameter(String key, String value) {
    this.initParameters.put(key, value);
    return this;
  }

  public YggdrasilServletDeploymentInfo addServlet(YggdrasilServletInfo yggdrasilServletInfo) {
    this.servlets.add(yggdrasilServletInfo);
    return this;
  }

  public YggdrasilServletDeploymentInfo addWelcomePage(String welcomePage) {
    this.welcomePages.add(welcomePage);
    return this;
  }

  public boolean isEagerFilterInit() {
    return eagerFilterInit;
  }

  public YggdrasilServletDeploymentInfo setEagerFilterInit(boolean eagerFilterInit) {
    this.eagerFilterInit = eagerFilterInit;
    return this;
  }

  public Map<String, String> getInitParameters() {
    return initParameters;
  }

  public List<Class<? extends EventListener>> getListeners() {
    return listeners;
  }

  public List<YggdrasilServletInfo> getServlets() {
    return servlets;
  }

  public List<String> getWelcomePages() {
    return welcomePages;
  }

  public Map<String, Object> getServletContextAttributes() {
    return servletContextAttributes;
  }

  public void addContextObject(String name, Object object) {
    this.contextObjects.put(name, object);
  }

  public Object getContextObject(String name) {
    return this.contextObjects.get(name);
  }
}
