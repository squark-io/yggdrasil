package org.dynamicjar.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
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
import java.util.List;
import java.util.Map;

class MavenHelper {
    static final String USER_HOME = System.getProperty("user.home");

    private static final File USER_MAVEN_CONFIGURATION_HOME = new File(USER_HOME, ".m2");

    private static final File DEFAULT_USER_SETTINGS_FILE =
        new File(USER_MAVEN_CONFIGURATION_HOME, "settings.xml");

    private static final File DEFAULT_GLOBAL_SETTINGS_FILE =
        new File(System.getProperty("M2_HOME", System.getProperty("maven.home", "")),
            "conf/settings.xml");
    private static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";

    private static Logger logger = LoggerFactory.getLogger(MavenHelper.class);

    static List<File> getDependencyFiles(InputStream projectPom)
        throws DependencyResolutionException {
        try {
            MavenProject mavenProject = loadProject(projectPom);
            return getDependencyFiles(mavenProject);
        } catch (IOException | DependencyResolutionException | XmlPullParserException |
            SettingsBuildingException | DependencyCollectionException e) {
            throw new DependencyResolutionException(e);
        }
    }

    private static List<File> getDependencyFiles(MavenProject mavenProject)
        throws DependencyCollectionException, DependencyResolutionException,
        SettingsBuildingException {

        Settings mavenSettings = getMavenSettings();

        RepositorySystem repositorySystem = getNewRepositorySystem();
        RepositorySystemSession repositorySystemSession =
            newRepositorySystemSession(repositorySystem, getLocalRepository(mavenSettings));

        List<RemoteRepository> remoteRepositories = getRemoteRepositories(mavenSettings);
        if (remoteRepositories.size() == 0) {
            RemoteRepository centralRepository =
                new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/")
                    .build();
            remoteRepositories.add(centralRepository);
        }

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
                        repositorySystemSession, remoteRepositories);
                } catch (DependencyCollectionException | org.eclipse.aether.resolution
                    .DependencyResolutionException e) {
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

    private static Settings getMavenSettings() throws SettingsBuildingException {

        String overrideUserSettings = System.getProperty("maven.settings");
        File overrideUserSettingsFile =
            StringUtils.isNotEmpty(overrideUserSettings) ? new File(overrideUserSettings) : null;

        SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
        settingsBuildingRequest.setSystemProperties(System.getProperties());
        settingsBuildingRequest.setUserSettingsFile(
            overrideUserSettingsFile != null ? overrideUserSettingsFile :
            DEFAULT_USER_SETTINGS_FILE);
        settingsBuildingRequest.setGlobalSettingsFile(DEFAULT_GLOBAL_SETTINGS_FILE);

        SettingsBuildingResult settingsBuildingResult;
        DefaultSettingsBuilderFactory mvnSettingBuilderFactory =
            new DefaultSettingsBuilderFactory();
        DefaultSettingsBuilder settingsBuilder = mvnSettingBuilderFactory.newInstance();
        settingsBuildingResult = settingsBuilder.build(settingsBuildingRequest);

        return settingsBuildingResult.getEffectiveSettings();
    }

    private static List<RemoteRepository> getRemoteRepositories(Settings mavenSettings) {
        Map<String, Profile> mavenProfiles = mavenSettings.getProfilesAsMap();
        List<RemoteRepository> remoteRepositories = new ArrayList<>();
        for (String activeProfile : mavenSettings.getActiveProfiles()) {
            Profile profile = mavenProfiles.get(activeProfile);
            List<Repository> profileRepositories = profile.getRepositories();
            for (Repository repository : profileRepositories) {
                RemoteRepository remoteRepository =
                    new RemoteRepository.Builder(repository.getId(), "default", repository.getUrl())
                        .build();
                remoteRepositories.add(remoteRepository);
            }
        }
        return remoteRepositories;
    }

    private static LocalRepository getLocalRepository(Settings mavenSettings) {
        String overrideLocalRepository = System.getProperty(MAVEN_LOCAL_REPOSITORY);
        String localRepository = mavenSettings.getLocalRepository();
        if (StringUtils.isEmpty(localRepository)) {
            localRepository = USER_HOME + "/.m2/repository";
        }
        return new LocalRepository(
            StringUtils.isNotEmpty(overrideLocalRepository) ? overrideLocalRepository :
            localRepository);
    }

    private static Collection<Artifact> resolveDependencies(Artifact defaultArtifact,
        RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession,
        List<RemoteRepository> remoteRepositories) throws DependencyCollectionException,
        org.eclipse.aether.resolution.DependencyResolutionException {

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
        RepositorySystem repositorySystem, LocalRepository localRepository) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        session.setLocalRepositoryManager(
            repositorySystem.newLocalRepositoryManager(session, localRepository));

        return session;
    }

    private static RepositorySystem getNewRepositorySystem() {
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