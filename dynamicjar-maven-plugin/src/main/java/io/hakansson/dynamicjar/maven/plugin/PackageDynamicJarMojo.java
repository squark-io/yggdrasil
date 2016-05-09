package io.hakansson.dynamicjar.maven.plugin;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;
import io.hakansson.dynamicjar.core.api.model.ProviderConfiguration;
import io.hakansson.dynamicjar.core.api.util.Scopes;
import io.hakansson.dynamicjar.maven.provider.api.DynamicJarDependencyMavenUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-03.
 * Copyright 2016
 */
@Mojo(name = "package-dynamicjar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageDynamicJarMojo extends AbstractMojo {

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;
    @Parameter(property = "dynamicjar.classesDir", defaultValue = "classes")
    private String classesDir;
    @Parameter(property = "dynamicjar.configFile",
        defaultValue = "classes/META-INF/dynamicjar.json")
    private String configFile;
    @Parameter(property = "dynamicjar.dependencyResolutionProvider", defaultValue = "maven")
    private String dependencyResolutionProviderString;
    /**
     * The MavenProject object.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Component
    private MavenProjectHelper mavenProjectHelper;
    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    @Parameter(defaultValue = "${repositorySystem}")
    @Component
    private RepositorySystem repositorySystem;
    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;
    /**
     * The project's remote repositories to use for the resolution of plugins and their
     * dependencies.
     *
     * @parameter default-value="${project.remotePluginRepositories}"
     * @readonly
     */
    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;
    @Parameter(property = "dynamicjar.manifest")
    private Map<String, Object> manifest;

    @Parameter(property = "dynamicjar.mainClass")
    private String mainClass;

    @Parameter(property = "dynamicjar.exclusions")
    private List<String> exclusions;

    @Parameter(property = "dynamicjar.includeLogger", defaultValue = "true")
    private boolean includeLogger;

    @Parameter(property = "dynamicjar.providerConfigurations")
    private HashSet<ProviderConfiguration> providerConfigurations;

    @Parameter(property = "dynamicjar.loadTransitiveProvidedDependencies", defaultValue = "false")
    private boolean loadTransitiveProvidedDependencies;

    private List<String> addedResources = new ArrayList<>();

    private Multimap<String, String> duplicates = HashMultimap.create();
    private Map<String, AddedTarget> addedJars = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File configFile = buildConfigurationFile();
        Manifest manifest = generateAndReturnManifest();
        try (JarOutputStream targetJarOutputStream = createTargetJar(manifest)) {
            addProjectArtifactAndConfig(targetJarOutputStream, configFile);
            addCompileDependencies(targetJarOutputStream);
            addSelfDependencies(targetJarOutputStream);
            targetJarOutputStream.close();
            logDuplicates();
            mavenProjectHelper.attachArtifact(project, project.getArtifact().getType(), "dynamicjar",
                new File(outputDirectory + "/" + getTargetJarName()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create target jar", e);
        }
    }

    private void addProjectArtifactAndConfig(JarOutputStream targetJarOutputStream, File configFile)
        throws MojoExecutionException {
        File baseDir = new File(outputDirectory, classesDir);
        if (baseDir.exists()) {
            try {
                File artifactFile;
                if ((artifactFile = project.getArtifact().getFile()) == null) {
                    throw new MojoExecutionException("Could not find project artifact. Ran goal before package phase?");
                }
                String name = project.getArtifactId() + "-" + project.getVersion();
                JarEntry localJarEntry = new JarEntry(Constants.LIB_PATH + name + "-classes.jar");
                localJarEntry.setLastModifiedTime(FileTime.fromMillis(artifactFile.lastModified()));
                addResource(new FileInputStream(artifactFile), localJarEntry, targetJarOutputStream);
                JarEntry configFileEntry = new JarEntry("META-INF/" + configFile.getName());
                configFileEntry.setLastModifiedTime(FileTime.fromMillis(artifactFile.lastModified()));
                addResource(new FileInputStream(configFile), configFileEntry, targetJarOutputStream);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to build local dependency jar", e);
            }
        }
    }

    private String getTargetJarName() {
        return project.getArtifactId() + "-" + project.getVersion() + "-dynamicjar." +
            project.getPackaging();
    }

    private JarOutputStream createTargetJar(Manifest manifest) throws IOException {
        String jarName = getTargetJarName();
        return new JarOutputStream(new FileOutputStream(outputDirectory + "/" + jarName), manifest);
    }

    /**
     * modified from http://stackoverflow.com/a/1281295/961395
     */
    private void add(JarFile sourceJarFile, JarEntry sourceJarEntry, JarOutputStream targetJarOutputStream,
        String sourceName) throws MojoExecutionException {
        if (sourceJarEntry.isDirectory()) {
            addDirectory(sourceJarEntry.getName(), targetJarOutputStream);
        } else {
            addResource(sourceJarFile, sourceJarEntry, targetJarOutputStream, sourceName);
        }
    }

    /*
    Partly stolen from
    https://github.com/apache/maven-plugins/blob/trunk/maven-shade-plugin/src/main/java/org
    /apache/maven/plugins/shade/DefaultShader.java @ 2016-03-24
     */
    private void logDuplicates() {

        Multimap<Collection<String>, String> overlapping = HashMultimap.create(20, 15);

        for (String file : duplicates.keySet()) {
            Collection<String> resources = duplicates.get(file);
            if (resources.size() > 1) {
                overlapping.put(resources, file);
            }
        }

        getLog().warn("Some resources are contained in two or more JARs. This is usually safe put may cause"
            + " undefined behaviour if different versions of resources are expected");
        for (Collection<String> jarz : overlapping.keySet()) {
            List<String> jarzStrings = new LinkedList<>();

            for (String file : jarz) {
                jarzStrings.add(file);
            }

            List<String> classes = overlapping.get(jarz).stream().map(clazz -> clazz.replace(".class", ""))
                .collect(Collectors.toCollection(LinkedList::new));

            getLog().warn(Joiner.on(", ").join(jarzStrings) + " define " + classes.size() +
                " overlapping resource(s): ");
            int max = 10;
            for (int i = 0; i < Math.min(max, classes.size()); i++) {
                getLog().warn("  - " + classes.get(i));
            }

            if (classes.size() > max) {
                getLog().warn("  - " + (classes.size() - max) + " more...");
            }

        }
    }

    private void addResource(File file, JarOutputStream targetJarOutputStream, String targetName)
        throws MojoExecutionException {
        if (addedResources.contains(file.getName())) {
            return;
        }
        addedResources.add(file.getName());
        JarEntry targetJarEntry = new JarEntry(targetName);
        targetJarEntry.setTime(file.lastModified());
        try (FileInputStream inputStream = new FileInputStream(file)) {
            addResource(inputStream, targetJarEntry, targetJarOutputStream);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to add jar " + file.getPath(), e);
        }
    }

    private void addResource(JarFile sourceJarFile, JarEntry sourceJarEntry, JarOutputStream targetJarOutputStream,
        String sourceName) throws MojoExecutionException {
        String name = sourceJarEntry.getName();
        duplicates.put(name, sourceName);
        if (addedResources.contains(name)) {
            return;
        }
        if (name.matches("META-INF\\/.*\\.(RSA|SF|DSA)")) {
            getLog().warn("Excluding " + name + " to workaround signature issues.");
            return;
        }
        JarEntry targetJarEntry = new JarEntry(name);
        targetJarEntry.setTime(sourceJarEntry.getTime());
        try (InputStream in = sourceJarFile.getInputStream(sourceJarEntry)) {
            addResource(in, targetJarEntry, targetJarOutputStream);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy jar entry " + name + " from jar " +
                sourceJarFile.getName(), e);
        }
        addedResources.add(name);
    }

    private void addResource(InputStream in, JarEntry targetJarEntry, JarOutputStream targetJarOutputStream)
        throws IOException {
        targetJarOutputStream.putNextEntry(targetJarEntry);
        IOUtil.copy(in, targetJarOutputStream);
        targetJarOutputStream.closeEntry();
    }

    private void addDirectory(String name, JarOutputStream targetJarOutputStream) throws MojoExecutionException {
        if (name.lastIndexOf('/') > 0) {
            String parent = name.substring(0, name.lastIndexOf('/'));

            if (!addedResources.contains(parent)) {
                addDirectory(parent, targetJarOutputStream);
            }
        }
        if (!name.endsWith("/")) {
            name += "/";
        }
        if (addedResources.contains(name)) {
            return;
        }

        JarEntry entry = new JarEntry(name);
        try {
            targetJarOutputStream.putNextEntry(entry);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to add directory " + name, e);
        }
        addedResources.add(name);
    }

    private Manifest generateAndReturnManifest() throws MojoExecutionException {
        File manifestFile = new File(new File(outputDirectory, classesDir), "META-INF/MANIFEST.MF");
        Manifest manifest;
        if (manifestFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(manifestFile);
                manifest = new Manifest(fileInputStream);
                fileInputStream.close();
            } catch (IOException e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
        } else {
            manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            manifest.getMainAttributes().put(new Attributes.Name("Build-Jdk"), System.getProperty("java.version"));
        }
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, Constants.DYNAMIC_JAR_BOOTSTRAP_CLASS_NAME);
        manifest.getMainAttributes().put(new Attributes.Name("DynamicJar-Version"), pluginDescriptor.getVersion());
        return manifest;
    }

    private File buildConfigurationFile() throws MojoExecutionException {

        DynamicJarConfiguration dynamicJarConfiguration = new DynamicJarConfiguration();
        DynamicJarDependency dynamicJarDependency = getProjectProvidedDependencies();
        dynamicJarConfiguration.setDependencies(dynamicJarDependency.getChildDependencies());
        dynamicJarConfiguration.setDynamicJarVersion(pluginDescriptor.getVersion());
        dynamicJarConfiguration.setLoadTransitiveProvidedDependencies(loadTransitiveProvidedDependencies);

        String classesJar = Constants.LIB_PATH + project.getArtifactId() + "-" + project.getVersion() + "-classes.jar";
        dynamicJarConfiguration.setClassesJar(classesJar);

        if (StringUtils.isNotEmpty(mainClass)) {
            dynamicJarConfiguration.setMainClass(mainClass);
        }

        validateProviderConfigurations();
        dynamicJarConfiguration.setProviderConfigurations(providerConfigurations);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(dynamicJarConfiguration);

        File configurationFile = new File(outputDirectory, configFile);
        if (!configurationFile.exists()) {
            try {
                configurationFile.getParentFile().mkdirs();
                configurationFile.createNewFile();
            } catch (IOException e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(configurationFile);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage());
        }
        return configurationFile;
    }

    private void validateProviderConfigurations() throws MojoExecutionException {
        if (providerConfigurations != null) {
            for (ProviderConfiguration providerConfiguration : providerConfigurations) {
                if (providerConfiguration.getIdentifier() == null) {
                    throw new MojoExecutionException("ProviderConfiguration must supply identifier");
                }
                if (providerConfiguration.getProperties() == null) {
                    throw new MojoExecutionException("ProviderConfiguration must supply properties");
                }
            }
        }
    }

    private void addCompileDependencies(JarOutputStream jarOutputStream)
        throws MojoExecutionException, MojoFailureException, IOException {

        List<org.apache.maven.model.Dependency> dependencies = project.getDependencies();

        for (org.apache.maven.model.Dependency dependency : dependencies) {
            addDependency(dependency, jarOutputStream, true, false, Constants.LIB_PATH);
        }
    }

    private void addDependency(org.apache.maven.model.Dependency dependency, JarOutputStream jarOutputStream,
        boolean asJar, boolean addAsRef, String pathIfJar) throws MojoExecutionException {
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(
            new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), null,
                dependency.getVersion(), new DefaultArtifactType(dependency.getType())), dependency.getScope()));
        collectRequest.setRepositories(remoteRepositories);
        try {
            CollectResult collectResult = repositorySystem.collectDependencies(repositorySystemSession, collectRequest);
            DependencyNode node = collectResult.getRoot();

            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setFilter(new ScopeDependencyFilter(null));
            dependencyRequest.setRoot(node);
            repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);

            if (asJar) {
                addNodeAsJar(node, jarOutputStream, addAsRef, pathIfJar);
            } else {
                addNodeAsClasses(node, jarOutputStream);
            }

        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void addSelfDependencies(JarOutputStream targetJarOutputStream)
        throws MojoFailureException, MojoExecutionException {
        addDependency(getCoreDependency(), targetJarOutputStream, false, false, null);
        addDependency(getApiDependency(), targetJarOutputStream, true, false, Constants.LIB_PATH);
        addDependency(getDependencyResolutionProvider(), targetJarOutputStream, true, true,
            Constants.DYNAMICJAR_RUNTIME_LIB_PATH);
        addDependency(getLoggingModule(), targetJarOutputStream, true, true, Constants.DYNAMICJAR_RUNTIME_LIB_PATH);
    }

    @SuppressWarnings("Duplicates")
    private org.apache.maven.model.Dependency getCoreDependency() {
        String dynamicJarVersion = pluginDescriptor.getVersion();
        org.apache.maven.model.Dependency coreDependency = new org.apache.maven.model.Dependency();
        coreDependency.setGroupId(Constants.DYNAMIC_JAR_GROUP_ID);
        coreDependency.setArtifactId(Constants.DYNAMIC_JAR_CORE_ARTIFACT_ID);
        coreDependency.setVersion(dynamicJarVersion);
        coreDependency.setScope(Scopes.COMPILE);
        return coreDependency;
    }

    @SuppressWarnings("Duplicates")
    private org.apache.maven.model.Dependency getApiDependency() {
        String dynamicJarVersion = pluginDescriptor.getVersion();
        org.apache.maven.model.Dependency apiDependency = new org.apache.maven.model.Dependency();
        apiDependency.setGroupId(Constants.DYNAMIC_JAR_GROUP_ID);
        apiDependency.setArtifactId(Constants.DYNAMIC_JAR_API_ARTIFACT_ID);
        apiDependency.setVersion(dynamicJarVersion);
        apiDependency.setScope(Scopes.COMPILE);
        return apiDependency;
    }

    private org.apache.maven.model.Dependency getDependencyResolutionProvider() throws MojoFailureException {
        String groupId;
        String artifactId;
        String version;
        switch (dependencyResolutionProviderString) {
            case "maven":
                groupId = Constants.DYNAMIC_JAR_MAVEN_PROVIDER_GROUP_ID;
                artifactId = Constants.DYNAMIC_JAR_MAVEN_PROVIDER_ARTIFACT_ID;
                version = pluginDescriptor.getVersion();
                break;
            default:
                String[] descriptor = dependencyResolutionProviderString.split(":");
                if (descriptor.length != 3) {
                    throw new MojoFailureException("Bad format dependencyResolutionProvider. Expected " +
                        "\"groupId:artifactId:version\". Got " +
                        dependencyResolutionProviderString);
                }
                groupId = descriptor[0];
                artifactId = descriptor[1];
                version = descriptor[2];
                break;
        }
        org.apache.maven.model.Dependency dependencyResolutionProviderDependency =
            new org.apache.maven.model.Dependency();
        dependencyResolutionProviderDependency.setGroupId(groupId);
        dependencyResolutionProviderDependency.setArtifactId(artifactId);
        dependencyResolutionProviderDependency.setVersion(version);
        dependencyResolutionProviderDependency.setScope(Scopes.COMPILE);
        return dependencyResolutionProviderDependency;
    }

    private org.apache.maven.model.Dependency getLoggingModule() {
        org.apache.maven.model.Dependency loggingModuleDependency = new org.apache.maven.model.Dependency();
        String dynamicJarVersion = pluginDescriptor.getVersion();
        loggingModuleDependency.setGroupId(Constants.DYNAMIC_JAR_LOGGING_MODULE_GROUP_ID);
        loggingModuleDependency.setArtifactId(Constants.DYNAMIC_JAR_LOGGING_MODULE_ARTIFACT_ID);
        loggingModuleDependency.setVersion(dynamicJarVersion);
        loggingModuleDependency.setScope(Scopes.COMPILE);
        return loggingModuleDependency;
    }

    private DynamicJarDependency getProjectProvidedDependencies() throws MojoExecutionException {

        List<org.apache.maven.model.Dependency> mavenDependencies = project.getDependencies();
        if (exclusions == null || exclusions.size() == 0) {
            exclusions = new ArrayList<>();
        }

        DynamicJarDependency rootDependency =
            new DynamicJarDependency(project.getGroupId(), project.getArtifactId(), null, project.getVersion(), null);

        Map<Artifact, String> dependencyArtifacts = new ConcurrentHashMap<>();
        for (org.apache.maven.model.Dependency dependency : mavenDependencies) {
            if (Scopes.PROVIDED.equals(dependency.getScope()) && !Boolean.parseBoolean(dependency.getOptional())) {
                dependencyArtifacts.put(
                    new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(),
                        null, dependency.getVersion(), new DefaultArtifactType(dependency.getType())),
                    dependency.getScope());
            }
        }
        DefaultRepositorySystemSession repositorySystemSession =
            new DefaultRepositorySystemSession(this.repositorySystemSession);
        DependencySelector depFilter = new AndDependencySelector(
            new ScopeDependencySelector(Scopes.TEST, Scopes.COMPILE, Scopes.RUNTIME, Scopes.SYSTEM),
            new OptionalDependencySelector(), new ExclusionDependencySelector());
        repositorySystemSession.setDependencySelector(depFilter);
        for (Map.Entry<Artifact, String> entry : dependencyArtifacts.entrySet()) {
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(entry.getKey(), entry.getValue()));
            collectRequest.setRepositories(remoteRepositories);
            DynamicJarDependency dynamicJarDependency;
            try {
                CollectResult collectResult =
                    repositorySystem.collectDependencies(repositorySystemSession, collectRequest);
                DependencyNode node = collectResult.getRoot();

                dynamicJarDependency = DynamicJarDependencyMavenUtil.fromDependencyNode(node, exclusions);

            } catch (Exception e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
            rootDependency.addChildDependency(dynamicJarDependency);
        }

        return rootDependency;
    }

    private void addNodeAsClasses(DependencyNode node, JarOutputStream targetJarOutputStream)
        throws MojoExecutionException {
        JarFile jar;
        if (!Scopes.COMPILE.equals(node.getDependency().getScope()) || node.getDependency().getOptional()) {
            return;
        }
        try {
            jar = new JarFile(node.getArtifact().getFile());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to get jar " + node.getArtifact().getFile().getPath(), e);
        }
        Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry sourceEntry = (JarEntry) enumEntries.nextElement();
            String name = sourceEntry.getName();
            if (name.startsWith("java/") || name.startsWith("com/sun/") ||
                name.startsWith("javax/") || "META-INF/INDEX.LIST".equals(name) ||
                "META-INF/MANIFEST.MF".equals(name)) {
                continue;
            }
            add(jar, sourceEntry, targetJarOutputStream, node.getArtifact().getFile().getName());
        }
        for (DependencyNode child : node.getChildren()) {
            addNodeAsClasses(child, targetJarOutputStream);
        }
    }

    private void addNodeAsJar(DependencyNode node, JarOutputStream targetJarOutputStream, boolean addAsRef, String path)
        throws MojoExecutionException {
        if (!Scopes.COMPILE.equals(node.getDependency().getScope()) || node.getDependency().getOptional()) {
            return;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        getLog().debug("Including dependency jar " + node.getArtifact().getFile().getPath());
        File file = node.getArtifact().getFile();
        String target = path + file.getName();
        if (addAsRef && addedJars.containsKey(file.getName())) {
            String refName = target.replace(".jar", ".ref");
            if (addedResources.contains(refName)) {
                return;
            }
            addedResources.add(refName);
            AddedTarget addedTarget = addedJars.get(file.getName());
            String refContent = addedTarget.path + addedTarget.name;
            InputStream refInputStream = new ByteArrayInputStream(refContent.getBytes());
            JarEntry targetJarEntry = new JarEntry(refName);
            try {
                addResource(refInputStream, targetJarEntry, targetJarOutputStream);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to add ref", e);
            }
        } else {
            addResource(file, targetJarOutputStream, target);
            addedJars.put(file.getName(), new AddedTarget(path, file.getName()));
        }
        for (DependencyNode child : node.getChildren()) {
            addNodeAsJar(child, targetJarOutputStream, addAsRef, path);
        }
    }

    private class AddedTarget {
        String path;
        String name;

        AddedTarget(String path, String name) {
            this.path = path;
            this.name = name;
        }
    }
}
