package io.hakansson.dynamicjar.maven.plugin;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;
import io.hakansson.dynamicjar.core.api.util.Scopes;
import io.hakansson.dynamicjar.maven.provider.api.DynamicJarDependencyMavenUtil;
import org.apache.commons.lang3.ArrayUtils;
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
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-03.
 * Copyright 2016
 */
@Mojo(name = "package-dynamicjar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageDynamicJarMojo extends AbstractMojo {

    private static final String dynamicJarGroupId = "io.hakansson.dynamicjar";
    private static final String dynamicJarArtifactId = "dynamicjar-core";
    private static final String dynamicJarMavenProviderGroupId = "io.hakansson.dynamicjar";
    private static final String dynamicJarMavenProviderArtifactId = "dynamicjar-maven-provider";
    private static final String dynamicJarClassName =
        "io.hakansson.dynamicjar.core.main.DynamicJar";
    private static final String MAVEN_DEPENDENCY_RESOLUTION_PROVIDER_CLASS =
        "io.hakansson.dynamicjar.maven.provider.MavenDependencyResolutionProvider";
    Multimap<String, String> duplicates = HashMultimap.create();
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
    private List<String> resources = new ArrayList<>();
    @Parameter(property = "dynamicjar.manifest")
    private Map<String, Object> manifest;
    @Parameter(property = "dynamicjar.mainClass")
    private String mainClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        buildConfigurationFile();
        Manifest manifest = generateAndReturnManifest();
        try (JarOutputStream targetJarOutputStream = createTargetJar(manifest)) {
            addLocalFiles(targetJarOutputStream);
            copyCompileDependencies(targetJarOutputStream);
            logDuplicates();
            mavenProjectHelper
                .attachArtifact(project, project.getArtifact().getType(), "dynamicjar",
                    new File(getTargetJarName()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create target jar", e);
        }
    }

    private void addLocalFiles(JarOutputStream targetJarOutputStream) throws MojoExecutionException {
        File baseDir = new File(outputDirectory, classesDir);
        if (baseDir.exists()) {
            addLocalFile(baseDir, baseDir, targetJarOutputStream);
        }
    }

    private void addLocalFile(File baseDir, File file, JarOutputStream targetJarOutputStream)
        throws MojoExecutionException {
        String relativePath = relativize(baseDir, file);
        duplicates.put(relativePath, getTargetJarName());
        if (file.isDirectory()) {
            addDirectory(relativePath, targetJarOutputStream);
            File[] files = file.listFiles();
            if (!ArrayUtils.isEmpty(files)) {
                for (File f : files) {
                    addLocalFile(baseDir, f, targetJarOutputStream);
                }
            }
        } else {
            try {
                if (resources.contains(relativePath)) {
                    return;
                }
                FileInputStream fileInputStream = new FileInputStream(file);
                JarEntry targetJarEntry = new JarEntry(relativePath);
                addResource(fileInputStream, targetJarEntry, targetJarOutputStream);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to open file " + file.getName(), e);
            }
        }
        resources.add(relativePath);
    }

    private String relativize(File baseDir, File file) {
        String relative = baseDir.toURI().relativize(file.toURI()).getPath();
        return relative;
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
    private void add(JarFile sourceJarFile, JarEntry sourceJarEntry,
        JarOutputStream targetJarOutputStream, String sourceName) throws MojoExecutionException {
        if (sourceJarEntry.isDirectory()) {
            addDirectory(sourceJarEntry.getName(), targetJarOutputStream);
        } else {
            addResource(sourceJarFile, sourceJarEntry, targetJarOutputStream, sourceName);
        }
    }

    /*
    Partly stolen from
    https://github.com/apache/maven-plugins/blob/trunk/maven-shade-plugin/src/main/java/org/apache/maven/plugins/shade/DefaultShader.java @ 2016-03-24
     */
    private void logDuplicates() {
        // CHECKSTYLE_OFF: MagicNumber
        Multimap<Collection<String>, String> overlapping = HashMultimap.create(20, 15);
        // CHECKSTYLE_ON: MagicNumber

        for (String file : duplicates.keySet()) {
            Collection<String> resources = duplicates.get(file);
            if (resources.size() > 1) {
                overlapping.put(resources, file);
            }
        }

        getLog().warn(
            "Some resources are contained in two or more JARs. This is usually safe put may cause" +
            " undefined behaviour if different versions of resources are expected");
        for ( Collection<String> jarz : overlapping.keySet() )
        {
            List<String> jarzStrings = new LinkedList<>();

            for ( String file : jarz )
            {
                jarzStrings.add( file );
            }

            List<String> classes = new LinkedList<>();

            for ( String clazz : overlapping.get( jarz ) )
            {
                classes.add( clazz.replace( ".class", "" ).replace( "/", "." ) );
            }

            getLog().warn(
                Joiner.on( ", " ).join( jarzStrings ) + " define " + classes.size() + " overlapping classes: " );

            int max = 10;

            for ( int i = 0; i < Math.min( max, classes.size() ); i++ )
            {
                getLog().warn( "  - " + classes.get( i ) );
            }

            if ( classes.size() > max )
            {
                getLog().warn( "  - " + ( classes.size() - max ) + " more..." );
            }

        }
    }

    private void addResource(JarFile sourceJarFile, JarEntry sourceJarEntry,
        JarOutputStream targetJarOutputStream, String sourceName) throws MojoExecutionException {
        String name = sourceJarEntry.getName();
        duplicates.put(name, sourceName);
        if (resources.contains(name)) {
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
        resources.add(name);
    }

    private void addResource(InputStream in, JarEntry targetJarEntry,
        JarOutputStream targetJarOutputStream) throws IOException {
        targetJarOutputStream.putNextEntry(targetJarEntry);
        IOUtil.copy(in, targetJarOutputStream);
        targetJarOutputStream.closeEntry();
    }

    private void addDirectory(String name, JarOutputStream targetJarOutputStream)
        throws MojoExecutionException {
        if (name.lastIndexOf('/') > 0) {
            String parent = name.substring(0, name.lastIndexOf('/'));

            if (!resources.contains(parent)) {
                addDirectory(parent, targetJarOutputStream);
            }
        }
        if (!name.endsWith("/")) {
            name += "/";
        }
        if (resources.contains(name)) {
            return;
        }

        JarEntry entry = new JarEntry(name);
        try {
            targetJarOutputStream.putNextEntry(entry);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to add directory " + name, e);
        }
        resources.add(name);
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

            manifest.getMainAttributes()
                .put(new Attributes.Name("Build-Jdk"), System.getProperty("java.version"));
        }
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, dynamicJarClassName);
        manifest.getMainAttributes()
            .put(new Attributes.Name("DynamicJar-Version"), pluginDescriptor.getVersion());
        return manifest;
    }

    private void buildConfigurationFile() throws MojoExecutionException {

        DynamicJarConfiguration dynamicJarConfiguration = new DynamicJarConfiguration();
        DynamicJarDependency dynamicJarDependency = getProjectProvidedDependencies();
        dynamicJarConfiguration.setDependencies(dynamicJarDependency.getChildDependencies());

        dynamicJarConfiguration.setDynamicJarVersion(pluginDescriptor.getVersion());

        if (StringUtils.isNotEmpty(mainClass)) {
            dynamicJarConfiguration.setMainClass(mainClass);
        }

        if ("maven".equals(dependencyResolutionProviderString)) {
            dynamicJarConfiguration
                .setDependencyResolutionProviderClass(MAVEN_DEPENDENCY_RESOLUTION_PROVIDER_CLASS);
        }

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
    }

    private void copyCompileDependencies(JarOutputStream jarOutputStream)
        throws MojoExecutionException, MojoFailureException, IOException {

        List<org.apache.maven.model.Dependency> dependencies = project.getDependencies();
        addSelfDependencies(dependencies);

        for (org.apache.maven.model.Dependency dependency : dependencies) {
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(
                new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getClassifier(), null, dependency.getVersion(),
                    new DefaultArtifactType(dependency.getType())), dependency.getScope()));
            collectRequest.setRepositories(remoteRepositories);
            try {
                CollectResult collectResult =
                    repositorySystem.collectDependencies(repositorySystemSession, collectRequest);
                DependencyNode node = collectResult.getRoot();

                DependencyRequest dependencyRequest = new DependencyRequest();
                dependencyRequest.setFilter(new ScopeDependencyFilter(null));
                dependencyRequest.setRoot(node);
                repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);

                copyNode(node, jarOutputStream);

            } catch (Exception e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
        }

        jarOutputStream.close();

    }

    private void addSelfDependencies(List<org.apache.maven.model.Dependency> dependencies)
        throws MojoFailureException {
        dependencies.add(getCoreDependency());
        dependencies.add(getDependencyResolutionProvider());
    }

    private org.apache.maven.model.Dependency getDependencyResolutionProvider()
        throws MojoFailureException {
        String groupId;
        String artifactId;
        String version;
        switch (dependencyResolutionProviderString) {
            case "maven":
                groupId = dynamicJarMavenProviderGroupId;
                artifactId = dynamicJarMavenProviderArtifactId;
                version = pluginDescriptor.getVersion();
                break;
            default:
                String[] descriptor = dependencyResolutionProviderString.split(":");
                if (descriptor.length != 3) {
                    throw new MojoFailureException(
                        "Bad format dependencyResolutionProvider. Expected " +
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

    private org.apache.maven.model.Dependency getCoreDependency() {
        String dynamicJarVersion = pluginDescriptor.getVersion();
        org.apache.maven.model.Dependency coreDependency = new org.apache.maven.model.Dependency();
        coreDependency.setGroupId(dynamicJarGroupId);
        coreDependency.setArtifactId(dynamicJarArtifactId);
        coreDependency.setVersion(dynamicJarVersion);
        coreDependency.setScope(Scopes.COMPILE);
        return coreDependency;
    }

    private DynamicJarDependency getProjectProvidedDependencies() throws MojoExecutionException {

        List<org.apache.maven.model.Dependency> mavenDependencies = project.getDependencies();
        DynamicJarDependency rootDependency =
            new DynamicJarDependency(project.getGroupId(), project.getArtifactId(), null,
                project.getVersion(), null);

        Map<Artifact, String> dependencyArtifacts = new ConcurrentHashMap<>();
        mavenDependencies.stream().forEach(dependency -> {
            if (Scopes.PROVIDED.equals(dependency.getScope())) {
                dependencyArtifacts.put(
                    new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                        dependency.getClassifier(), null, dependency.getVersion(),
                        new DefaultArtifactType(dependency.getType())), dependency.getScope());
            }
        });

        for (Map.Entry<Artifact, String> entry : dependencyArtifacts.entrySet()) {
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(entry.getKey(), entry.getValue()));
            collectRequest.setRepositories(remoteRepositories);
            DynamicJarDependency dynamicJarDependency;
            try {
                CollectResult collectResult =
                    repositorySystem.collectDependencies(repositorySystemSession, collectRequest);
                DependencyNode node = collectResult.getRoot();

                dynamicJarDependency = DynamicJarDependencyMavenUtil.fromDependencyNode(node);

            } catch (Exception e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
            rootDependency.addChildDependency(dynamicJarDependency);
        }

        return rootDependency;
    }

    private void copyNode(DependencyNode node, JarOutputStream targetJarOutputStream)
        throws MojoExecutionException {
        if (!Scopes.COMPILE.equals(node.getDependency().getScope())) {
            return;
        }
        getLog().debug("Including dependency jar " + node.getArtifact().getFile().getPath());
        JarFile jar;
        try {
            jar = new JarFile(node.getArtifact().getFile());
        } catch (IOException e) {
            throw new MojoExecutionException(
                "Failed to get jar " + node.getArtifact().getFile().getPath(), e);
        }
        Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry sourceEntry = (JarEntry) enumEntries.nextElement();
            if ("META-INF/INDEX.LIST".equals(sourceEntry.getName()) ||
                "META-INF/MANIFEST.MF".equals(sourceEntry.getName())) {
                continue;
            }

            add(jar, sourceEntry, targetJarOutputStream, node.getArtifact().getFile().getName());
        }
        for (DependencyNode child : node.getChildren()) {
            copyNode(child, targetJarOutputStream);
        }

    }
}
