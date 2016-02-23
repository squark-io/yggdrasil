package org.dynamicjar.core.main;

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
import org.dynamicjar.core.api.DependencyResolutionProvider;
import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.model.DynamicJarDependency;
import org.dynamicjar.core.api.util.LambdaExceptionUtil;
import org.dynamicjar.core.api.util.Scopes;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class MavenDependencyResolutionProvider implements DependencyResolutionProvider {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final File USER_MAVEN_CONFIGURATION_HOME = new File(USER_HOME, ".m2");
    private static final File DEFAULT_USER_SETTINGS_FILE =
        new File(USER_MAVEN_CONFIGURATION_HOME, "settings.xml");
    private static final File DEFAULT_GLOBAL_SETTINGS_FILE =
        new File(System.getProperty("M2_HOME", System.getProperty("maven.home", "")),
            "conf/settings.xml");
    private static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";

    private static Logger logger = LoggerFactory.getLogger(MavenDependencyResolutionProvider.class);

    @NotNull
    private static MavenProject loadProject(final InputStream pomFile)
        throws IOException, XmlPullParserException, DependencyResolutionException {
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        if (pomFile != null) {
            Model model = mavenReader.read(pomFile);
            return new MavenProject(model);
        }

        throw new DependencyResolutionException("Failed to find pom.xml");
    }

    private static DynamicJarDependency getDependencyFiles(final MavenProject mavenProject)
        throws DependencyCollectionException, DependencyResolutionException,
        SettingsBuildingException {

        Settings mavenSettings = getMavenSettings();

        //@TODO add repositores from MavenProject
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

        DynamicJarDependency rootDynamicJarDependency =
            new DynamicJarDependency(mavenProject.getGroupId(), mavenProject.getArtifactId(),
                mavenProject.getPackaging(), mavenProject.getVersion(), mavenProject.getFile());

        List<org.apache.maven.model.Dependency> dependencies = mavenProject.getDependencies();

        dependencies.parallelStream()
            .filter(dependency -> Scopes.PROVIDED.equalsIgnoreCase(dependency.getScope()))
            .forEach(LambdaExceptionUtil.rethrowConsumer(dependency -> {
                try {
                    Artifact dependencyArtifact =
                        new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                            dependency.getClassifier(), dependency.getType(),
                            dependency.getVersion());
                    DynamicJarDependency dynamicJarDependency =
                        resolveDependencies(dependencyArtifact, repositorySystem,
                            repositorySystemSession, remoteRepositories);
                    if (dynamicJarDependency == null) {
                        logger.error("No dependencies found");
                        throw new DependencyResolutionException("No dependencies found");
                    }
                    rootDynamicJarDependency.addChildDependency(dynamicJarDependency);
                } catch (DependencyCollectionException | org.eclipse.aether.resolution
                    .DependencyResolutionException e) {
                    logger.error("Failed to retrieve dependency", e);
                }
            }));

        return rootDynamicJarDependency;
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

    private static RepositorySystem getNewRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newRepositorySystemSession(
        final RepositorySystem repositorySystem, final LocalRepository localRepository) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        session.setLocalRepositoryManager(
            repositorySystem.newLocalRepositoryManager(session, localRepository));

        return session;
    }

    private static LocalRepository getLocalRepository(final Settings mavenSettings) {
        String overrideLocalRepository = System.getProperty(MAVEN_LOCAL_REPOSITORY);
        String localRepository = mavenSettings.getLocalRepository();
        if (StringUtils.isEmpty(localRepository)) {
            localRepository = USER_HOME + "/.m2/repository";
        }
        return new LocalRepository(
            StringUtils.isNotEmpty(overrideLocalRepository) ? overrideLocalRepository :
            localRepository);
    }

    private static List<RemoteRepository> getRemoteRepositories(final Settings mavenSettings) {
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

        return fromDependencyNode(node);
    }

    private static DynamicJarDependency fromDependencyNode(final DependencyNode dependencyNode) {
        Artifact artifact = dependencyNode.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();
        File file = artifact.getFile();
        String scope = dependencyNode.getDependency().getScope();
        Set<DynamicJarDependency> children = new HashSet<>();
        if (isNotEmpty(dependencyNode.getChildren())) {
            dependencyNode.getChildren().parallelStream().forEach(child -> {
                children.add(fromDependencyNode(child));
            });
        }
        return new DynamicJarDependency(groupId, artifactId, extension, classifier, version, file,
            scope, children, Scopes.PROVIDED);
    }

    @Override
    public final DynamicJarDependency resolveDependencies(final InputStream projectPom)
        throws DependencyResolutionException {
        try {
            MavenProject mavenProject = loadProject(projectPom);
            return getDependencyFiles(mavenProject);
        } catch (IOException | XmlPullParserException |
            SettingsBuildingException | DependencyCollectionException e) {
            throw new DependencyResolutionException(e);
        }
    }

    @Override
    public final InputStream getDependencyDescriberFor(final String groupId,
        final String artifactId) {
        String path = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
        return MavenDependencyResolutionProvider.class.getResourceAsStream(path);
    }

    @Override
    public DynamicJarDependency resolveDependency(DynamicJarDependency dependency)
        throws DependencyResolutionException {

        Artifact aetherArtifact =
            new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                dependency.getClassifier(), dependency.getExtension(), dependency.getVersion());

        RepositorySystem repositorySystem = getNewRepositorySystem();
        Settings mavenSettings = null;
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
}