package org.dynamicjar.core.main;

import org.apache.commons.lang3.StringUtils;
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
import org.dynamicjar.core.api.DependencyResolutionProvider;
import org.dynamicjar.core.api.DynamicJarDependencyMavenUtil;
import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.model.DynamicJarDependency;
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
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.traverser.StaticDependencyTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MavenDependencyResolutionProvider implements DependencyResolutionProvider {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final File USER_MAVEN_CONFIGURATION_HOME = new File(USER_HOME, ".m2");
    private static final File DEFAULT_USER_SETTINGS_FILE =
        new File(USER_MAVEN_CONFIGURATION_HOME, "settings.xml");
    private static final File DEFAULT_GLOBAL_SETTINGS_FILE =
        new File(System.getProperty("M2_HOME", System.getProperty("maven.home", "")),
            "conf/settings.xml");
    private static final String MAVEN_LOCAL_REPOSITORY = "maven.repo.local";

    private static Logger logger = LoggerFactory.getLogger(MavenDependencyResolutionProvider.class);
    private static RepositorySystem repositorySystem;
    private static Settings mavenSettings;
    private static DefaultRepositorySystemSession repositorySystemSession;
    private static LocalRepository localRepository;
    private static ArrayList<RemoteRepository> remoteRepositories;

    @Override
    public DynamicJarDependency resolveDependency(DynamicJarDependency dependency)
        throws DependencyResolutionException {

        Artifact aetherArtifact =
            new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                dependency.getClassifier(), dependency.getExtension(), dependency.getVersion());

        RepositorySystem repositorySystem = getNewRepositorySystem();
        Settings mavenSettings;
        try {
            mavenSettings = getMavenSettings();
        } catch (SettingsBuildingException e) {
            throw new DependencyResolutionException(e);
        }
        RepositorySystemSession repositorySystemSession =
            newRepositorySystemSession(repositorySystem, getLocalRepository(mavenSettings));
        List<RemoteRepository> remoteRepositories = getRemoteRepositories(mavenSettings);
        try {
            return resolveDependencies(aetherArtifact, repositorySystem, repositorySystemSession,
                remoteRepositories);
        } catch (DependencyCollectionException | org.eclipse.aether.resolution
            .DependencyResolutionException e) {
            throw new DependencyResolutionException(e);
        }
    }

    private static DynamicJarDependency resolveDependencies(final Artifact defaultArtifact,
        final RepositorySystem repositorySystem,
        final RepositorySystemSession repositorySystemSession,
        final List<RemoteRepository> remoteRepositories) throws DependencyCollectionException,
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

        return DynamicJarDependencyMavenUtil.fromDependencyNode(node);
    }

    private static RepositorySystem getNewRepositorySystem() {
        if (repositorySystem == null) {
            DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
            locator.addService(RepositoryConnectorFactory.class,
                BasicRepositoryConnectorFactory.class);
            locator.addService(TransporterFactory.class, FileTransporterFactory.class);
            locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
            repositorySystem = locator.getService(RepositorySystem.class);
        }
        return repositorySystem;
    }

    private static Settings getMavenSettings() throws SettingsBuildingException {

        if (mavenSettings == null) {
            String overrideUserSettings = System.getProperty("maven.settings");
            File overrideUserSettingsFile =
                StringUtils.isNotEmpty(overrideUserSettings) ? new File(overrideUserSettings) :
                null;

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

            mavenSettings = settingsBuildingResult.getEffectiveSettings();
        }
        return mavenSettings;
    }

    private static RepositorySystemSession newRepositorySystemSession(
        final RepositorySystem repositorySystem, final LocalRepository localRepository) {
        if (repositorySystemSession == null) {
            repositorySystemSession = MavenRepositorySystemUtils.newSession();
            repositorySystemSession.setDependencyTraverser(new StaticDependencyTraverser(true));
            repositorySystemSession.setLocalRepositoryManager(repositorySystem
                .newLocalRepositoryManager(repositorySystemSession, localRepository));
        }

        return repositorySystemSession;
    }

    private static LocalRepository getLocalRepository(final Settings mavenSettings) {
        if (localRepository == null) {
            String localRepositoryString = System.getProperty(MAVEN_LOCAL_REPOSITORY);
            if (StringUtils.isEmpty(localRepositoryString)) {
                logger.debug("No local repository override. Using default.");
                localRepositoryString = mavenSettings.getLocalRepository();
                if (StringUtils.isEmpty(localRepositoryString)) {
                    localRepositoryString = USER_HOME + "/.m2/repository";
                }
            }
            logger.debug("Using local repository '" + localRepositoryString + "'.");
            localRepository = new LocalRepository(localRepositoryString);
        }

        return localRepository;
    }

    private static List<RemoteRepository> getRemoteRepositories(final Settings mavenSettings) {
        if (remoteRepositories == null) {
            Map<String, Profile> mavenProfiles = mavenSettings.getProfilesAsMap();
            remoteRepositories = new ArrayList<>();
            for (String activeProfile : mavenSettings.getActiveProfiles()) {
                Profile profile = mavenProfiles.get(activeProfile);
                List<Repository> profileRepositories = profile.getRepositories();
                for (Repository repository : profileRepositories) {
                    RemoteRepository remoteRepository =
                        new RemoteRepository.Builder(repository.getId(), "default",
                            repository.getUrl()).build();
                    remoteRepositories.add(remoteRepository);
                }
            }
        }
        return remoteRepositories;
    }
}