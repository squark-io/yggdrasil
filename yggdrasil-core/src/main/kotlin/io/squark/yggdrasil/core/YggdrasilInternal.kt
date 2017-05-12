package io.squark.yggdrasil.core

import io.squark.yggdrasil.bootstrap.YggdrasilClassLoader
import io.squark.yggdrasil.core.cdi.ServletBeanObserver
import io.squark.yggdrasil.core.context.YggdrasilContext
import io.squark.yggdrasil.core.context.YggdrasilInitialContextFactory
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.ServletContainerInitializerInfo
import io.undertow.servlet.handlers.DefaultServlet
import io.undertow.servlet.util.ImmediateInstanceFactory
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.DefaultConfiguration
import org.jboss.resteasy.cdi.CdiInjectorFactory
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters
import org.jboss.weld.environment.se.Weld
import org.jboss.weld.environment.servlet.WeldServletLifecycle
import java.util.ServiceLoader
import java.util.jar.Manifest
import javax.enterprise.inject.se.SeContainerInitializer
import javax.enterprise.inject.spi.BeanManager
import javax.naming.Binding
import javax.naming.Context
import javax.naming.InitialContext
import javax.servlet.ServletContainerInitializer
import javax.servlet.annotation.HandlesTypes
import org.jboss.weld.environment.servlet.Listener as WeldListener

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2017-03-24.
 * Copyright 2017
 */

private const val DELEGATED_MAIN_CLASS = "Delegated-Main-Class"

class YggdrasilInternal {

  private var logger: Logger? = null
  private var isClassLoaderValidated = false

  private fun _initialize(args: Array<String>, messagesMap: Map<String, List<String>>) {
    validateClassLoader()
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, YggdrasilInitialContextFactory::class.java.name)

    setupLogging()
    logger!!.info("Initializing Yggdrasil")
    for ((level, messages) in messagesMap) {
      for (message in messages) {
        logger!!.log(Level.getLevel(level), message)
      }
    }

    val context: YggdrasilContext = InitialContext().lookup("") as YggdrasilContext
    context.createSubcontext("java")
    context.createSubcontext("java:comp")

    bootstrapCDI(context)
    loadUndertow(context)

    logger!!.info("Yggdrasil initiated")

    getClassLoader().getResource("META-INF/MANIFEST.MF")?.let {
      it.openStream().use {
        val manifest = Manifest(it)
        manifest.mainAttributes.getValue(DELEGATED_MAIN_CLASS)?.let {
          loadDelegatedClass(it, args)
        }
      }
    }
  }

  private fun loadDelegatedClass(className: String, args: Array<String>) {
    logger!!.info("Found $DELEGATED_MAIN_CLASS in Manifest. Loading $className")
    val delegateClass = try {
      getClassLoader().loadClass(className)
    } catch (e: ClassNotFoundException) {
      throw YggdrasilException("Could not find class $className from Manifest $DELEGATED_MAIN_CLASS", e)
    }
    val mainMethod = try {
      delegateClass.getMethod("main", Array<String>::class.java)
    } catch (e: NoSuchMethodException) {
      throw YggdrasilException("Class $className has no valid main method", e)
    }
    mainMethod.invoke(null, args)
    logger!!.info("$className loaded")
  }

  private fun getClassLoader(): YggdrasilClassLoader {
    validateClassLoader()
    return javaClass.classLoader as YggdrasilClassLoader
  }

  private fun validateClassLoader() {
    if (!isClassLoaderValidated) {
      if (javaClass.classLoader !is YggdrasilClassLoader) {
        throw YggdrasilException("ClassLoader is not instance of ${YggdrasilClassLoader::class.java}. " +
          "Found ${javaClass.classLoader::class.java}. Please initiate Yggdrasil with Yggdrasil.initialize(...) or Main-Class")
      }
    } else {
      isClassLoaderValidated = true
    }
  }

  private fun setupLogging() {
    val suffixes = listOf("properties", "yaml", "yml", "json", "json")
    var foundConfig = false
    for (suffix in suffixes) {
      if (Thread.currentThread().contextClassLoader.getResource("log4j2.${suffix}") != null) {
        foundConfig = true
        break
      }
    }
    if (!foundConfig) {
      Configurator.initialize(DefaultConfiguration())
      Configurator.setRootLevel(Level.INFO)
    }
    logger = LogManager.getLogger(YggdrasilInternal::class.java)
  }

  private fun bootstrapCDI(context: YggdrasilContext) {
    val containerInitializer = SeContainerInitializer.newInstance()
    containerInitializer.addProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, false)
    //containerInitializer.addProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, true)
    val container = containerInitializer.initialize()
    context.bind("java:comp/BeanManager", Binding("BeanManager", BeanManager::class.java.name, container.beanManager))
  }

  private fun loadUndertow(context: YggdrasilContext) {
    val beanManager = context.lookup("java:comp/BeanManager") as BeanManager
    val servletBeanObserver: ServletBeanObserver = beanManager.getExtension(ServletBeanObserver::class.java)
    val deployment = Servlets.deployment().let {
      it.classLoader = object {}.javaClass.classLoader
      it.contextPath = "/"
      it.deploymentName = "yggdrasil"
      it.addServlet(Servlets.servlet("default", DefaultServlet::class.java))
      it.addListener(Servlets.listener(WeldListener::class.java))
      it.addInitParameter(WeldServletLifecycle::class.java.`package`.name + ".archive.isolation", "false")
      it.addServletContextAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME,
        context.lookup("java:comp/BeanManager"))
    }
    deployment.addListeners(servletBeanObserver.listeners)
    deployment.addServlets(servletBeanObserver.servlets)
    for (triple in servletBeanObserver.filters) {
      deployment.addFilter(triple.first)
      for (dispatcherType in triple.third) {
        for ((type, mapping) in triple.second) {
          when (type) {
            ServletBeanObserver.FilterMappingType.URL -> deployment.addFilterUrlMapping(triple.first.name, mapping,
              dispatcherType)
            ServletBeanObserver.FilterMappingType.Servlet -> deployment.addFilterServletNameMapping(triple.first.name,
              mapping, dispatcherType)
          }
        }
      }
    }
    val servletContainerInitializers = servletBeanObserver.servletContainerInitializers.mapNotNull { original ->
      val classes = mutableSetOf<Class<*>>()
      original.handlesTypes?.forEach { `class` ->
        when {
          `class`.isAnnotation -> {
            beanManager.getBeans(Any::class.java).forEach { bean ->
              if (bean.beanClass.annotations.map { it.annotationClass.java }.contains(`class`)) {
                classes.add(bean.beanClass)
              }
            }
          }
          else -> beanManager.getBeans(`class`).forEach { bean ->
            classes.add(bean.beanClass)
          }
        }
      }
      ServletContainerInitializerInfo(original.servletContainerInitializerClass, classes)
    }
    deployment.addServletContainerInitalizers(servletContainerInitializers)

    val servletContainerInitializerServices = ServiceLoader.load(ServletContainerInitializer::class.java)
    for (initializer in servletContainerInitializerServices) {
      val types: MutableList<Class<out Any>> = mutableListOf()
      for (annotation in initializer.javaClass.annotations) {
        if (annotation is HandlesTypes) {
          for (`class` in annotation.value) {
            types.add(`class`.java)
          }
          break
        }
      }
      val classes = mutableSetOf<Class<out Any>>()
      for (`class` in types) {
        when {
          `class`.isAnnotation -> {
            beanManager.getBeans(Any::class.java).forEach { bean ->
              if (bean.beanClass.annotations.map { it.annotationClass.java }.contains(`class`)) {
                classes.add(bean.beanClass)
              }
            }
          }
          else -> beanManager.getBeans(`class`).forEach { bean ->
            classes.add(bean.beanClass)
          }
        }
      }
      val info = ServletContainerInitializerInfo(initializer.javaClass,
        ImmediateInstanceFactory<ServletContainerInitializer>(initializer), classes)
      deployment.addServletContainerInitalizer(info)
    }

    deployment.addInitParameter("resteasy.injector.factory", CdiInjectorFactory::class.java.name)
    deployment.addInitParameter(ResteasyContextParameters.RESTEASY_USE_BUILTIN_PROVIDERS, "false")
    val manager = Servlets.defaultContainer().addDeployment(deployment)
    manager.deploy()
    val servletHandler: HttpHandler = manager.start()
    val server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(servletHandler).build()
    server.start()
  }
}

class YggdrasilException(message: String?, cause: Throwable? = null) : Exception(message, cause)
