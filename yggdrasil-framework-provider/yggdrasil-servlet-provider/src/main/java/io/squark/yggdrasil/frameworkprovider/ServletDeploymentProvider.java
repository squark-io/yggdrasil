package io.squark.yggdrasil.frameworkprovider;

import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.ServletProvider;
import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.YggdrasilServletDeploymentInfo;
import io.squark.yggdrasil.core.api.YggdrasilServletInfo;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.ProviderConfiguration;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.util.StatusCodes;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDIProvider;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

public class ServletDeploymentProvider implements FrameworkProvider {

  private static final String DEFAULT_PORT = "8080";
  private static final String PROPERTY_PORT = "port";
  private static final String PROPERTY_CONTEXT_PATH = "contextPath";
  private static final String PROPERTY_DEPLOYMENT_NAME = "deploymentName";
  private static final String DEFAULT_CONTEXT_PATH = "/";
  private static final String DEFAULT_DEPLOYMENT_NAME = "yggdrasil-deployment";

  @Override
  public void provide(YggdrasilConfiguration configuration) throws YggdrasilException {

    String port = DEFAULT_PORT;
    String contextPath = DEFAULT_CONTEXT_PATH;
    String deploymentName = DEFAULT_DEPLOYMENT_NAME;
    if (configuration.getProviderConfigurations().isPresent()) {
      for (ProviderConfiguration providerConfiguration : configuration.getProviderConfigurations().get()) {
        if (providerConfiguration.getIdentifier().equals(ServletDeploymentProvider.class.getSimpleName())) {
          port = (String) providerConfiguration.getProperties().getOrDefault(PROPERTY_PORT, DEFAULT_PORT);
          contextPath = (String) providerConfiguration.getProperties().getOrDefault(PROPERTY_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
          deploymentName =
            (String) providerConfiguration.getProperties().getOrDefault(PROPERTY_DEPLOYMENT_NAME, DEFAULT_DEPLOYMENT_NAME);
          break;
        }
      }
    }

    ServiceLoader<ServletProvider> servletProviders = ServiceLoader.load(ServletProvider.class);

    List<String> mappings = new ArrayList<>();
    BeanManager beanManager = (BeanManager) YggdrasilContext.getObject(BeanManager.class.getName());

    DeploymentInfo deployment =
      Servlets.deployment().setClassLoader(this.getClass().getClassLoader()).setContextPath(contextPath)
        .setDeploymentName(deploymentName).addListener(Servlets.listener(org.jboss.weld.environment.servlet.Listener.class))
        .addInitParameter(WeldServletLifecycle.class.getPackage().getName() + ".archive.isolation", "false")
        .addServletContextAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, beanManager);


    Map<ServletProvider, YggdrasilServletDeploymentInfo> deploymentInfoMap = new HashMap<>();
    for (ServletProvider servletProvider : servletProviders) {
      YggdrasilServletDeploymentInfo di = servletProvider.initialize(beanManager, configuration);
      di.getServlets().forEach(yggdrasilServletInfo -> {
        ServletInfo servletInfo = convertServletInfo(yggdrasilServletInfo);
        deployment.addServlet(servletInfo);
        mappings.addAll(servletInfo.getMappings());
      });
      di.getWelcomePages().forEach(deployment::addWelcomePage);
      di.getInitParameters().forEach(deployment::addInitParameter);
      di.getListeners().forEach(listenerClass -> deployment.addListener(Servlets.listener(listenerClass)));
      di.getServletContextAttributes().forEach(deployment::addServletContextAttribute);
      deploymentInfoMap.put(servletProvider, di);
    }

    DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
    manager.deploy();
    HttpHandler servletHandler;
    try {
      servletHandler = manager.start();
    } catch (ServletException e) {
      throw new YggdrasilException(e);
    }

    List<Pattern> patterns = new ArrayList<>();
    for (String mapping : mappings) {
      String[] split = mapping.split("/");
      StringBuilder regex = new StringBuilder();
      int i = 0;
      for (String s : split) {
        if (s.equals("*")) {
          if (i == split.length - 1) {
            regex.append("[/]?.*");
          } else {
            regex.append("/.+");
          }
        } else if (!s.isEmpty()) {
          regex.append("/").append(s);
        }
        i++;
      }
      patterns.add(Pattern.compile(regex.toString()));
    }

    HttpHandler handler = exchange -> {
      for (Pattern pattern : patterns) {
        if (pattern.matcher(exchange.getRequestURI()).matches()) {
          servletHandler.handleRequest(exchange);
          return;
        }
      }
      exchange.setStatusCode(StatusCodes.NOT_FOUND).endExchange();
      //resourceHandler.handleRequest(exchange);
    };
    Undertow server = Undertow.builder().addHttpListener(Integer.parseInt(port), "localhost").setHandler(handler).build();
    server.start();
    deploymentInfoMap.forEach((servletProvider, yggdrasilServletDeploymentInfo) -> servletProvider.postInitialize(
      yggdrasilServletDeploymentInfo, configuration));
  }

  @Override
  public String getName() {
    return ServletDeploymentProvider.class.getSimpleName();
  }

  @Override
  public List<ProviderDependency> runAfter() {
    return Collections.singletonList(new ProviderDependency(CDIProvider.class.getSimpleName(), false));
  }

  private ServletInfo convertServletInfo(YggdrasilServletInfo servlet) {
    ServletInfo result =
      Servlets.servlet(servlet.getName(), servlet.getClazz()).setLoadOnStartup(servlet.getLoadOnStartup())
        .setAsyncSupported(servlet.getAsyncSupported()).setRequireWelcomeFileMapping(servlet.getRequireWelcomeFileMapping());
    for (String mapping : servlet.getMappings()) {
      result.addMapping(mapping);
    }
    return result;
  }


  private static class CombinedResourceManager implements ResourceManager {

    private List<ResourceManager> resourceManagers = new ArrayList<>();

    public CombinedResourceManager(ResourceManager... managers) {
      for (ResourceManager manager : managers) {
        resourceManagers.add(manager);
      }
    }

    @Override
    public void close() throws IOException {
      for (ResourceManager manager : resourceManagers) {
        manager.close();
      }
    }

    @Override
    public Resource getResource(String path) throws IOException {
      Resource resource = null;
      Iterator<ResourceManager> resourceManagerIterator = resourceManagers.iterator();
      while (resource == null && resourceManagerIterator.hasNext()) {
        resource = resourceManagerIterator.next().getResource(path);
      }
      return resource;
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
      for (ResourceManager resourceManager : resourceManagers) {
        if (resourceManager.isResourceChangeListenerSupported()) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener listener) {
      for (ResourceManager resourceManager : resourceManagers) {
        if (resourceManager.isResourceChangeListenerSupported()) {
          resourceManager.registerResourceChangeListener(listener);
        }
      }
    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener listener) {
      for (ResourceManager resourceManager : resourceManagers) {
        resourceManager.removeResourceChangeListener(listener);
      }
    }
  }
}
