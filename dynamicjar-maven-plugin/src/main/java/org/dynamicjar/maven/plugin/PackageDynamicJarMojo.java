package org.dynamicjar.maven.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.dynamicjar.core.api.DynamicJarDependencyMavenUtil;
import org.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.dynamicjar.core.api.model.DynamicJarDependency;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-03.
 * Copyright 2016
 */
@Mojo(name = "package-dynamicjar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageDynamicJarMojo extends AbstractMojo {

    /**
     * The MavenProject object.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    @Parameter(defaultValue = "${repositorySystem}")
    @Component
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins and their
     * dependencies.
     *
     * @parameter default-value="${project.remotePluginRepositories}"
     * @readonly
     */
    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DynamicJarDependency dynamicJarDependency = getProjectDependencies();

        DynamicJarConfiguration dynamicJarConfiguration = new DynamicJarConfiguration();
        getLog().info(project.getDependencies().toString());
        dynamicJarConfiguration.setDependencies(dynamicJarDependency.getChildDependencies());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(dynamicJarConfiguration);


        File configurationFile = new File(outputDirectory, "classes/META-INF/dynamicjar.json");
        if (!configurationFile.exists()) {
            try {
                configurationFile.getParentFile().mkdirs();
                configurationFile.createNewFile();
            } catch (IOException e) {
                getLog().error(e);
                throw new MojoExecutionException(e.getMessage());
            }
        }
        getLog().info("testd");
        getLog().info(configurationFile.getAbsolutePath());
        try {
            FileOutputStream outputStream = new FileOutputStream(configurationFile);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException(e.getMessage());
        }

    }

    private DynamicJarDependency getProjectDependencies() throws MojoExecutionException {

        List<org.apache.maven.model.Dependency> mavenDependencies = project.getDependencies();
        DynamicJarDependency rootDependency =
            new DynamicJarDependency(project.getGroupId(), project.getArtifactId(), null,
                project.getVersion(), null);

        Map<String, Artifact> dependencyArtifacts = new HashMap<>();
        mavenDependencies.parallelStream().forEach(dependency -> {
            dependencyArtifacts.put(dependency.getScope(),
                new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getClassifier(), null, dependency.getVersion(),
                    new DefaultArtifactType(dependency.getType())));
        });

        for (Map.Entry<String, Artifact> entry : dependencyArtifacts.entrySet()) {
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(entry.getValue(), entry.getKey()));
            collectRequest.setRepositories(remoteRepos);
            DynamicJarDependency dynamicJarDependency = null;
            try {
                CollectResult collectResult =
                    repoSystem.collectDependencies(repoSession, collectRequest);
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
}
