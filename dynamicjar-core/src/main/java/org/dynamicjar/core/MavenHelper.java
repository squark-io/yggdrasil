package org.dynamicjar.core;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class MavenHelper {

    private static Logger logger = LoggerFactory.getLogger(MavenHelper.class);

    static List<File> getDependencyFiles(InputStream projectPom,
        File localRepoFolder)
        throws IOException, XmlPullParserException, DependencyCollectionException,
        DependencyResolutionException {
        MavenProject mavenProject = loadProject(projectPom);

        return getDependencyFiles(mavenProject, localRepoFolder);
    }

    private static List<File> getDependencyFiles(MavenProject mavenProject,
        File localRepository) throws DependencyCollectionException, DependencyResolutionException {

        RepositorySystem repositorySystem = newRepositorySystem();
        RepositorySystemSession repositorySystemSession =
            newRepositorySystemSession(repositorySystem, localRepository);


        //GET SETTINGS: http://stackoverflow
        // .com/questions/27818659/loading-mavens-settings-xml-for-jcabi-aether-to-use
        RemoteRepository centralRepository =
            new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/")
                .build();


        List<org.apache.maven.model.Dependency> dependencies = mavenProject.getDependencies();
        final List<File> dependencyFiles = new ArrayList<>();

        dependencies.parallelStream()
            .filter(dependency -> JavaScopes.PROVIDED.equalsIgnoreCase(dependency.getScope()))
            .forEach(dependency -> {
                Collection<Artifact> resolvedDependencies = null;
                try {
                    Artifact dependencyArtifact =
                        new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                            dependency.getClassifier(), dependency.getType(),
                            dependency.getVersion());
                    resolvedDependencies = resolveDependencies(dependencyArtifact, repositorySystem,
                        repositorySystemSession, Collections.singletonList(centralRepository));
                } catch (DependencyCollectionException | DependencyResolutionException e) {
                    logger.error("Failed to retrieve dependency", e);
                }

                if (resolvedDependencies != null) {
                    resolvedDependencies.forEach(artifact -> {
                        if (artifact.getFile() != null) {
                            dependencyFiles.add(artifact.getFile());
                        } else {
                            logger.warn("Failed to resolve file for artifact " + artifact);
                        }
                    });
                } else {
                    logger.error("No dependencies found");
                }

            });

        return dependencyFiles;
    }

    private static Collection<Artifact> resolveDependencies(Artifact defaultArtifact,
        RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession, List<RemoteRepository> remoteRepositories)
        throws DependencyCollectionException, DependencyResolutionException {

        Dependency dependency = new Dependency((defaultArtifact), null);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        remoteRepositories.forEach(collectRequest::addRepository);
        DependencyNode node =
            repositorySystem.collectDependencies(repositorySystemSession, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setFilter(new ScopeDependencyFilter(null));
        dependencyRequest.setRoot(node);
        repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);

        return nlg.getArtifacts(true);
    }

    private static RepositorySystemSession newRepositorySystemSession(
        RepositorySystem repositorySystem, File localRepositoryFile) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepository = new LocalRepository(localRepositoryFile);
        session.setLocalRepositoryManager(
            repositorySystem.newLocalRepositoryManager(session, localRepository));

        return session;
    }

    private static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService(RepositorySystem.class);
    }

    private static MavenProject loadProject(InputStream pomFile)
        throws IOException, XmlPullParserException {
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        if (pomFile != null) {
            Model model = mavenReader.read(pomFile);
            return new MavenProject(model);
        }

        return null;
    }
}