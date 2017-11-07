package io.squark.yggdrasil.maven

import org.apache.commons.io.IOUtils
import org.apache.maven.RepositoryUtils
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.DefaultDependencyResolutionRequest
import org.apache.maven.project.MavenProject
import org.apache.maven.project.MavenProjectHelper
import org.apache.maven.project.ProjectDependenciesResolver
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.ScopeDependencyFilter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/**
 * Yggdrasil Maven Plugin
 *
 * Plugin for creating Yggdrasil applications conforming to the microprofile.io standard
 * @see <a href="http://microprofile.io">microprofile.io</a>
 *
 * Created by Erik Håkansson on 2017-03-25.
 * Copyright 2017
 *
 */
@Mojo(name = "yggdrasil", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PACKAGE)
class YggdrasilMojo : AbstractMojo() {

  /**
   * The MavenProject object.

   * @parameter expression="${project}"
   * *
   * @readonly
   */
  @Parameter(defaultValue = "\${project}", readonly = true, required = true)
  private lateinit var project: MavenProject

  @Parameter(defaultValue = "\${plugin}", readonly = true) // Maven 3 only
  private lateinit var plugin: PluginDescriptor

  @Component
  private lateinit var dependenciesResolver: ProjectDependenciesResolver

  @Component
  private lateinit var repositorySystem: RepositorySystem

  @Parameter(defaultValue = "\${repositorySystemSession}", readonly = true, required = true)
  private lateinit var repositorySystemSession: RepositorySystemSession

  @Component
  private lateinit var mavenProjectHelper: MavenProjectHelper
  private var classesDir: File? = null
  private val addedResources = mutableListOf<String>()
  private val addedDeps = mutableMapOf<InternalDependencyCoordinate, Dependency>()

  private val EMPTY_BEANS_XML = "META-INF/standins/empty-beans.xml"
  private val BEANS_XML = "META-INF/beans.xml"
  private val YGGDRASIL_LIBS_PATH = "META-INF/libs/"
  private val YGGDRASIL_MAIN_CLASS = "io.squark.yggdrasil.bootstrap.Yggdrasil"
  private val DELEGATED_MAIN_CLASS = "Delegated-Main-Class"

  /**
   * Performs packaging of the Yggdrasil jar
   *
   * @throws MojoExecutionException if an unexpected problem occurs.
   * Throwing this exception causes a "BUILD ERROR" message to be displayed.
   * @throws MojoFailureException if an expected problem (such as a compilation failure) occurs.
   * Throwing this exception causes a "BUILD FAILURE" message to be displayed.
   */
  override fun execute() {
    classesDir = File(project.build.outputDirectory)
    val manifest = generateAndReturnManifest()
    addedResources += JarFile.MANIFEST_NAME
    val targetFile = File(project.build.directory, getTargetJarName())
    val targetJar = createTargetJar(targetFile, manifest)
    log.info("Building jar: ${targetFile.absolutePath}")
    targetJar.use {
      addFile(classesDir!!, null, it)
      val beansXML = File(classesDir, BEANS_XML)
      if (!beansXML.exists()) {
        addBeansXml(it)
      }
      addYggdrasilDependencies(it)
      addDependencies(it)
    }
    targetJar.close()
    mavenProjectHelper.attachArtifact(project, project.artifact.type, "yggdrasil", targetFile)
    log.info("Built jar: ${targetFile.absolutePath}")
  }

  private fun addYggdrasilDependencies(jarOutputStream: JarOutputStream) {
    val yggdrasilArtifacts = plugin.artifacts.filter { it.groupId == "io.squark.yggdrasil" }.associateBy { it.artifactId }

    val bootstrapDependency = RepositoryUtils.toDependency(yggdrasilArtifacts["yggdrasil-bootstrap"]!!, null)
    val coreDependency = RepositoryUtils.toDependency(yggdrasilArtifacts["yggdrasil-core"]!!, null)
    val collectRequest = CollectRequest(listOf(bootstrapDependency, coreDependency), null,
      project.remoteProjectRepositories)
    val result = repositorySystem.resolveDependencies(repositorySystemSession,
      DependencyRequest(collectRequest, ScopeDependencyFilter(listOf(JavaScopes.COMPILE), null)))

    val bootstrapNode = result.root.children.filter { it.artifact.artifactId == "yggdrasil-bootstrap" }.first()
    val bootstrapDeps = listOf(bootstrapNode.dependency) + getDependencyChildren(bootstrapNode)
    bootstrapDeps.forEach {
      addedDeps.put(InternalDependencyCoordinate(it.artifact.groupId, it.artifact.artifactId, it.artifact.classifier,
        it.artifact.extension), it)
      if (it.artifact.file == null || !it.artifact.file.exists()) {
        throw MojoExecutionException("Couldn't find artifact file for ${it.artifact}")
      }
      val jarFile = JarFile(it.artifact.file)
      explodeJar(jarFile, jarOutputStream)
    }

    val coreNode = result.root.children.filter { it.artifact.artifactId == "yggdrasil-core" }.first()
    addFile(coreNode.artifact.file, null, jarOutputStream, YGGDRASIL_LIBS_PATH)
    addedDeps.put(InternalDependencyCoordinate(coreNode.artifact.groupId, coreNode.artifact.artifactId,
      coreNode.artifact.classifier, coreNode.artifact.extension), coreNode.dependency)
    addDependencyGraph(coreNode, jarOutputStream)
  }

  private fun explodeJar(jarFile: JarFile, jarOutputStream: JarOutputStream) {
    jarFile.entries().iterator().forEachRemaining {
      log.debug("Writing ${it.name}")
      if (!it.isDirectory) {
        if (addedResources.contains(it.name)) {
          log.debug("$it.name already written. Skipping.")
        } else {
          addedResources += it.name
          jarOutputStream.putNextEntry(it)
          val jarInputStream = jarFile.getInputStream(it)
          val buffer = ByteArray(2048)
          var len = jarInputStream.read(buffer)
          while (len > 0) {
            jarOutputStream.write(buffer, 0, len)
            len = jarInputStream.read(buffer)
          }
          jarOutputStream.closeEntry()
        }
      }
    }
  }

  private fun addDependencies(jarOutputStream: JarOutputStream) {
    val resolutionRequest = DefaultDependencyResolutionRequest(project, repositorySystemSession)
    val result = dependenciesResolver.resolve(resolutionRequest)
    if (result.collectionErrors.size > 0) {
      throw MojoFailureException(
        "Failed to resolve some dependencies: \n${result.collectionErrors.joinToString(separator = "\n")}")
    }
    addDependencyGraph(result.dependencyGraph, jarOutputStream)
  }

  private fun addDependencyGraph(dependencyGraph: DependencyNode,
                                 jarOutputStream: JarOutputStream) {
    val flatList = getDependencyChildren(dependencyGraph)
    val addedPlusNew = addedDeps.values + flatList
    val duplicates = getDependencyDuplicates(addedPlusNew)
    duplicates.forEach {
      val versions = it.value.joinToString(transform = { it.artifact.version })
      log.warn(
        "Found differing dependency versions for \"${it.key}\". Will use the first one found. Versions: $versions")
    }
    flatList.forEach {
      val coordinate = InternalDependencyCoordinate(it.artifact.groupId, it.artifact.artifactId, it.artifact.classifier,
        it.artifact.extension)
      if (!addedDeps.keys.contains(coordinate)) {
        if (it.artifact.file == null) {
          throw MojoFailureException("Failed to resolve dependency $it")
        }
        addFile(it.artifact.file, null, jarOutputStream, YGGDRASIL_LIBS_PATH)
        addedDeps.put(coordinate, it)
      }
    }
  }

  private fun getDependencyDuplicates(flatList: List<Dependency>): Map<InternalDependencyCoordinate, List<Dependency>> {
    val grouped = flatList.groupBy {
      InternalDependencyCoordinate(it.artifact.groupId, it.artifact.artifactId, it.artifact.classifier,
        it.artifact.extension)
    }
    return grouped.filterValues { deps ->
      deps.size > 1 && deps.any { it.artifact.version != deps[0].artifact.version }
    }
  }

  private fun getDependencyChildren(dependencyNode: DependencyNode): List<Dependency> {
    val flatList = dependencyNode.children.map { it.dependency }.filter { it.scope == JavaScopes.COMPILE }.toMutableList()
    dependencyNode.children.forEach { flatList.addAll(getDependencyChildren(it)) }
    return flatList
  }

  private fun addBeansXml(jarOutputStream: JarOutputStream) {
    val standin = this::class.java.classLoader.getResourceAsStream(EMPTY_BEANS_XML)
    val jarEntry = JarEntry(BEANS_XML)
    log.debug("Writing ${jarEntry.name}")
    jarOutputStream.putNextEntry(jarEntry)
    IOUtils.copy(standin, jarOutputStream)
    jarOutputStream.closeEntry()
  }

  private fun addFile(file: File, prefix: String?, jarOutputStream: JarOutputStream, targetPath: String? = null) {
    if (file.isDirectory) {
      file.listFiles().forEach { addFile(it, prefix, jarOutputStream) }
    } else {
      addEntry(file, prefix, jarOutputStream, targetPath)
    }
  }

  private fun addEntry(file: File, prefix: String?, jarOutputStream: JarOutputStream, targetPath: String?) {
    val name = if (targetPath == null) {
      "${prefix ?: ""}${classesDir!!.toPath().relativize(file.toPath())}"
    } else {
      var slash = ""
      if (!targetPath.endsWith('/')) slash = "/"
      "${prefix ?: ""}$targetPath$slash${file.toPath().fileName}"
    }
    log.debug("Writing $name")
    if (addedResources.contains(name)) {
      log.debug("$name already written. Skipping.")
      return
    }
    addedResources += name
    val jarEntry = JarEntry(name)
    val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    jarEntry.let {
      it.creationTime = attributes.creationTime()
      it.lastAccessTime = attributes.lastAccessTime()
      it.lastModifiedTime = attributes.lastModifiedTime()
    }
    jarOutputStream.putNextEntry(jarEntry)
    IOUtils.copy(file.inputStream(), jarOutputStream)
    jarOutputStream.closeEntry()
  }

  private fun getTargetJarName(): String {
    return project.artifactId + "-" + project.version + "-yggdrasil." + project.packaging
  }

  @Throws(IOException::class)
  private fun createTargetJar(targetFile: File, manifest: Manifest): JarOutputStream {
    return JarOutputStream(FileOutputStream(targetFile), manifest)
  }

  @Throws(MojoExecutionException::class)
  private fun generateAndReturnManifest(): Manifest {
    val manifestFile = File(project.build.outputDirectory, JarFile.MANIFEST_NAME)
    val manifest: Manifest
    if (manifestFile.exists()) {
      try {
        val fileInputStream = FileInputStream(manifestFile)
        manifest = Manifest(fileInputStream)
        fileInputStream.close()
      } catch (e: IOException) {
        log.error(e)
        throw MojoExecutionException(e.message)
      }
      if (manifest.mainAttributes[Attributes.Name.MAIN_CLASS] != null) {
        manifest.mainAttributes.putValue(DELEGATED_MAIN_CLASS,
          manifest.mainAttributes[Attributes.Name.MAIN_CLASS].toString())
      }
    } else {
      manifest = Manifest()
    }
    if (!manifest.mainAttributes.containsKey(Attributes.Name.MANIFEST_VERSION.toString())) {
      manifest.mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    }
    if (!manifest.mainAttributes.containsKey("Build-Jdk")) {
      manifest.mainAttributes.put(Attributes.Name("Build-Jdk"), System.getProperty("java.version"))
    }
    manifest.mainAttributes.put(Attributes.Name.MAIN_CLASS, YGGDRASIL_MAIN_CLASS)
    return manifest
  }

  private data class InternalDependencyCoordinate(val groupId: String, val artifactId: String, val classifier: String,
                                                  val type: String)
}
