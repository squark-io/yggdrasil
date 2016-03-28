package io.hakansson.dynamicjar.maven.provider;

import io.hakansson.dynamicjar.core.api.DependencyResolutionProvider;
import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;
import io.hakansson.dynamicjar.maven.provider.api.DynamicJarDependencyMavenUtil;
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
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
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
        logger.debug("Using remote repositories: " + remoteRepositories);
        try {
            return resolveDependencies(aetherArtifact, repositorySystem, repositorySystemSession,
                remoteRepositories);
        } catch (DependencyCollectionException | org.eclipse.aether.resolution
            .DependencyResolutionException e) {
            throw new DependencyResolutionException(e);
        }
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

            File settingsFileToUse = overrideUserSettingsFile != null ? overrideUserSettingsFile :
                                     DEFAULT_USER_SETTINGS_FILE;
            logger.debug("Using Maven settings file " + settingsFileToUse.getPath());

            SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
            settingsBuildingRequest.setSystemProperties(System.getProperties());
            settingsBuildingRequest.setUserSettingsFile(settingsFileToUse);
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

    private synchronized static RepositorySystemSession newRepositorySystemSession(
        final RepositorySystem repositorySystem, final LocalRepository localRepository) {
        if (repositorySystemSession == null) {
            repositorySystemSession = MavenRepositorySystemUtils.newSession();
            repositorySystemSession.setDependencyTraverser(new StaticDependencyTraverser(true));
            repositorySystemSession.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);
            LocalRepositoryManager localRepositoryManager = repositorySystem
                .newLocalRepositoryManager(repositorySystemSession, localRepository);
            if (localRepositoryManager == null) {
                logger.error("Failed to get localRepositoryManager");
            }
            repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
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
            remoteRepositories = new ArrayList<>();
        }
        Map<String, Profile> mavenProfiles = mavenSettings.getProfilesAsMap();
        List<String> activeMavenProfiles = mavenSettings.getActiveProfiles();
        if (activeMavenProfiles.size() == 0) {
            for (Map.Entry<String, Profile> profile : mavenProfiles.entrySet()) {
                if (profile.getValue().getActivation().isActiveByDefault()) {
                    activeMavenProfiles.add(profile.getKey());
                }
            }
        }
        logger.debug("Found the following active Maven profiles: " + activeMavenProfiles);
        for (String activeProfile : activeMavenProfiles) {
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
}