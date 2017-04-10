package io.squark.yggdrasil.core.cdi

import io.undertow.servlet.Servlets
import io.undertow.servlet.api.FilterInfo
import io.undertow.servlet.api.ListenerInfo
import io.undertow.servlet.api.ServletContainerInitializerInfo
import io.undertow.servlet.api.ServletInfo
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher
import java.lang.reflect.Modifier
import java.util.EventListener
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.inject.spi.AnnotatedType
import javax.enterprise.inject.spi.Extension
import javax.enterprise.inject.spi.ProcessAnnotatedType
import javax.enterprise.inject.spi.WithAnnotations
import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.Servlet
import javax.servlet.ServletContainerInitializer
import javax.servlet.annotation.HandlesTypes
import javax.servlet.annotation.WebFilter
import javax.servlet.annotation.WebListener
import javax.servlet.annotation.WebServlet
import kotlin.reflect.KClass

@ApplicationScoped class ServletBeanObserver : Extension {

  //Blacklisted, most likely due to other initialization.
  val blackListedServlets = setOf<Class<*>>(HttpServlet30Dispatcher::class.java)

  val servlets: MutableList<ServletInfo> = mutableListOf()
  val filters: MutableList<Triple<FilterInfo, List<FilterMappingInfo>, Array<DispatcherType>>> = mutableListOf()
  val listeners: MutableList<ListenerInfo> = mutableListOf()
  val servletContainerInitializers: MutableList<ServletContainerInitializerInfo> = mutableListOf()

  fun observeWebServlets(@WithAnnotations(WebServlet::class) @Observes event: ProcessAnnotatedType<out Servlet>) {
    if (!blackListedServlets.contains(event.annotatedType.javaClass)) {
      servlets += toServletInfo(event.annotatedType)
    }
  }

  fun observeWebFilters(@WithAnnotations(WebFilter::class) @Observes event: ProcessAnnotatedType<out Filter>) {
    filters += toFilterInfo(event.annotatedType)
  }

  fun observeWebListeners(@WithAnnotations(
    WebListener::class) @Observes event: ProcessAnnotatedType<out EventListener>) {
    listeners += toListenerInfo(event.annotatedType)
  }

  fun observerServletContainerInitializers(@Observes event: ProcessAnnotatedType<out ServletContainerInitializer>) {
    if (!event.annotatedType.javaClass.isInterface && !Modifier.isAbstract(event.annotatedType.javaClass.modifiers))
      servletContainerInitializers += toServletContainerInitializerInfo(event.annotatedType)
  }

  private fun toServletContainerInitializerInfo(annotatedType: AnnotatedType<out ServletContainerInitializer>): ServletContainerInitializerInfo {
    val handlesTypesAnnotation: HandlesTypes? = annotatedType.getAnnotation(HandlesTypes::class.java)
    val arrayOfKClasses: Array<KClass<*>>? = handlesTypesAnnotation?.value
    val arrayOfClasses = arrayOfKClasses?.map { it.java }
    return ServletContainerInitializerInfo(annotatedType.javaClass, arrayOfClasses?.toSet())
  }

  private fun toListenerInfo(annotatedType: AnnotatedType<out EventListener>): ListenerInfo {
    return Servlets.listener(annotatedType.javaClass)
  }

  private fun toServletInfo(annotatedType: AnnotatedType<out Servlet>): ServletInfo {
    val webServletAnnotation = annotatedType.getAnnotation(WebServlet::class.java)
    val servletInfo = Servlets.servlet(webServletAnnotation.name, annotatedType.javaClass)
    servletInfo.addMappings(webServletAnnotation.value.asList())
    servletInfo.addMappings(webServletAnnotation.urlPatterns.asList())
    for (initParam in webServletAnnotation.initParams) servletInfo.initParams += initParam.name to initParam.value
    servletInfo.let {
      it.loadOnStartup = webServletAnnotation.loadOnStartup
      it.isAsyncSupported = webServletAnnotation.asyncSupported
    }
    return servletInfo
  }

  private fun toFilterInfo(annotatedType: AnnotatedType<out Filter>): Triple<FilterInfo, List<FilterMappingInfo>, Array<DispatcherType>> {
    val webFilterAnnotation = annotatedType.getAnnotation(WebFilter::class.java)
    val filterInfo = Servlets.filter(webFilterAnnotation.filterName, annotatedType.javaClass)
    for (initParam in webFilterAnnotation.initParams) filterInfo.initParams += initParam.name to initParam.value
    val filterMappingInfos = mutableListOf<FilterMappingInfo>()
    filterMappingInfos += webFilterAnnotation.value.map { FilterMappingInfo(FilterMappingType.URL, it) }
    filterMappingInfos += webFilterAnnotation.urlPatterns.map { FilterMappingInfo(FilterMappingType.URL, it) }
    filterMappingInfos += webFilterAnnotation.servletNames.map { FilterMappingInfo(FilterMappingType.Servlet, it) }
    filterInfo.isAsyncSupported = webFilterAnnotation.asyncSupported
    return Triple(filterInfo, filterMappingInfos, webFilterAnnotation.dispatcherTypes)
  }

  enum class FilterMappingType {
    URL, Servlet
  }

  data class FilterMappingInfo(val type: FilterMappingType, val mapping: String)
}
