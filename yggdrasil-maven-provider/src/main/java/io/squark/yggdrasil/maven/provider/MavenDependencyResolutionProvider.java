/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.maven.provider;

import io.squark.yggdrasil.core.api.DependencyResolutionProvider;
import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.api.model.YggdrasilDependency;
import io.squark.yggdrasil.core.api.util.Scopes;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.squark.yggdrasil.maven.provider.api.YggdrasilDependencyMavenUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Mirror;
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
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.traverser.StaticDependencyTraverser;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MavenDependencyResolutionProvider implements DependencyResolutionProvider {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final File USER_MAVEN_CONFIGURATION_HOME = new File(USER_HOME, ".m2");
    private static final File DEFAULT_USER_SETTINGS_FILE = new File(USER_MAVEN_CONFIGURATION_HOME, "settings.xml");
    private static final File DEFAULT_GLOBAL_SETTINGS_FILE = new File(
            System.getProperty("M2_HOME", System.getProperty("maven.home", "")), "conf/settings.xml");
    private static final String MAVEN_LOCAL_REPOSITORY = "maven.repo.local";

    private Logger logger = InternalLoggerBinder.getLogger(MavenDependencyResolutionProvider.class);
    private RepositorySystem repositorySystem;
    private Settings mavenSettings;
    private DefaultRepositorySystemSession repositorySystemSession;
    private LocalRepository localRepository;
    private ArrayList<RemoteRepository> remoteRepositories;

    @Override
    public Set<YggdrasilDependency> resolveDependencies(Set<YggdrasilDependency> dependencies,
            boolean loadTransitiveProvidedDependencies) throws DependencyResolutionException
    {

        List<Dependency> mavenDependencies = new ArrayList<>();
        for (YggdrasilDependency dependency : dependencies) {
            Artifact aetherArtifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                    dependency.getClassifier(), dependency.getExtension(), dependency.getVersion());
            Dependency mavenDependency = new Dependency(aetherArtifact, null);
            mavenDependencies.add(mavenDependency);
        }

        RepositorySystem repositorySystem = getNewRepositorySystem();
        Settings mavenSettings;
        try {
            mavenSettings = getMavenSettings();
        } catch (SettingsBuildingException e) {
            throw new DependencyResolutionException(e);
        }
        RepositorySystemSession repositorySystemSession = newRepositorySystemSession(repositorySystem,
                getLocalRepository(mavenSettings), mavenSettings, loadTransitiveProvidedDependencies);
        List<RemoteRepository> remoteRepositories = getRemoteRepositories(mavenSettings);

        if (logger.isDebugEnabled())
            logger.debug("Using remote repositories: " + Collections.synchronizedList(remoteRepositories));
        try {
            return resolveDependencies(null, mavenDependencies, repositorySystem, repositorySystemSession,
                    remoteRepositories).getChildDependencies();
        } catch (DependencyCollectionException | org.eclipse.aether.resolution.DependencyResolutionException e) {
            throw new DependencyResolutionException(e);
        }
    }

    private RepositorySystem getNewRepositorySystem() {
        if (repositorySystem == null) {
            DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
            locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
            locator.addService(TransporterFactory.class, FileTransporterFactory.class);
            locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
            repositorySystem = locator.getService(RepositorySystem.class);
        }
        return repositorySystem;
    }

    private Settings getMavenSettings() throws SettingsBuildingException {

        if (mavenSettings == null) {
            String overrideUserSettings = System.getProperty("maven.settings");
            File overrideUserSettingsFile = StringUtils.isNotEmpty(overrideUserSettings) ? new File(overrideUserSettings) : null;

            File settingsFileToUse = overrideUserSettingsFile != null ? overrideUserSettingsFile : DEFAULT_USER_SETTINGS_FILE;
            logger.debug("Using Maven settings file " + settingsFileToUse.getPath());

            SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
            settingsBuildingRequest.setSystemProperties(System.getProperties());
            settingsBuildingRequest.setUserSettingsFile(settingsFileToUse);
            settingsBuildingRequest.setGlobalSettingsFile(DEFAULT_GLOBAL_SETTINGS_FILE);

            SettingsBuildingResult settingsBuildingResult;
            DefaultSettingsBuilderFactory mvnSettingBuilderFactory = new DefaultSettingsBuilderFactory();
            DefaultSettingsBuilder settingsBuilder = mvnSettingBuilderFactory.newInstance();
            settingsBuildingResult = settingsBuilder.build(settingsBuildingRequest);

            mavenSettings = settingsBuildingResult.getEffectiveSettings();
        }
        return mavenSettings;
    }

    synchronized RepositorySystemSession newRepositorySystemSession(final RepositorySystem repositorySystem,
            final LocalRepository localRepository, Settings mavenSettings, boolean loadTransitiveProvidedDependencies) throws
            DependencyResolutionException
    {
        if (repositorySystemSession == null) {
            repositorySystemSession = MavenRepositorySystemUtils.newSession();
            String[] excludedScopes;
            if (loadTransitiveProvidedDependencies) {
                excludedScopes = new String[]{Scopes.TEST, Scopes.RUNTIME, Scopes.SYSTEM};
            } else {
                excludedScopes = new String[]{Scopes.TEST, Scopes.RUNTIME, Scopes.SYSTEM, Scopes.PROVIDED};
            }
            DependencySelector depFilter = new AndDependencySelector(new ScopeDependencySelector(excludedScopes),
                    new OptionalDependencySelector(), new ExclusionDependencySelector());
            repositorySystemSession.setDependencySelector(depFilter);
            repositorySystemSession.setDependencyTraverser(new StaticDependencyTraverser(true));
            repositorySystemSession.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);
            DefaultMirrorSelector mirrorSelector = new DefaultMirrorSelector();
            for (Mirror mirror : mavenSettings.getMirrors()) {mirrorSelector.add(mirror.getId(), mirror.getUrl(),
                    mirror.getLayout(), false, mirror.getMirrorOf(), mirror.getMirrorOfLayouts());
            }
            repositorySystemSession.setMirrorSelector(mirrorSelector);
            DefaultProxySelector proxySelector = new DefaultProxySelector();
            if (mavenSettings.getActiveProxy() != null) {
                Proxy aetherProxy = toAetherProxy(mavenSettings.getActiveProxy());
                proxySelector.add(aetherProxy, mavenSettings.getActiveProxy().getNonProxyHosts());
            } else if (mavenSettings.getProxies().size() > 0) {
                for (org.apache.maven.settings.Proxy proxy : mavenSettings.getProxies()) {
                    Proxy aetherProxy = toAetherProxy(proxy);
                    proxySelector.add(aetherProxy, proxy.getNonProxyHosts());
                }
            } else {
                String httpProxy = getEnv("http_proxy");
                String httpsProxy = getEnv("https_proxy");
                if (httpProxy != null) {
                    Proxy proxy = toAetherProxy(httpProxy);
                    proxySelector.add(proxy, getEnv("no_proxy"));
                }
                if (httpsProxy != null) {
                    Proxy proxy = toAetherProxy(httpsProxy);
                    proxySelector.add(proxy, getEnv("no_proxy"));
                }
            }
            repositorySystemSession.setProxySelector(proxySelector);
            LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(repositorySystemSession,
                    localRepository);
            if (localRepositoryManager == null) {
                logger.error("Failed to get localRepositoryManager");
            }
            repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
        }

        return repositorySystemSession;
    }

    private Proxy toAetherProxy(String proxyString) throws DependencyResolutionException {
        URL proxyUrl;
        try {
            proxyUrl = new URL(proxyString);
        } catch (MalformedURLException e) {
            throw new DependencyResolutionException(e);
        }
        Authentication authentication = null;
        if (proxyUrl.getUserInfo() != null) {
            String userInfo = proxyUrl.getUserInfo();
            String authority = proxyUrl.getAuthority();
            authentication = new AuthenticationBuilder().addUsername(userInfo).addPassword(authority).build();
        }
        Proxy proxy;
        if (authentication != null) {
            proxy = new Proxy(proxyUrl.getProtocol(), proxyUrl.getHost(), proxyUrl.getPort(), authentication);
        } else {
            proxy = new Proxy(proxyUrl.getProtocol(), proxyUrl.getHost(), proxyUrl.getPort());
        }
        return proxy;
    }

    private Proxy toAetherProxy(org.apache.maven.settings.Proxy mavenProxy) {
        Authentication authentication = new AuthenticationBuilder().addUsername(mavenProxy.getUsername()).addPassword(
                mavenProxy.getPassword()).build();
        return new Proxy(mavenProxy.getProtocol(), mavenProxy.getHost(), mavenProxy.getPort(), authentication);
    }

    private LocalRepository getLocalRepository(final Settings mavenSettings) {
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

    private List<RemoteRepository> getRemoteRepositories(final Settings mavenSettings) {
        if (remoteRepositories == null) {
            remoteRepositories = new ArrayList<>();
        }
        Map<String, Profile> mavenProfiles = mavenSettings.getProfilesAsMap();
        List<String> activeMavenProfiles = mavenSettings.getActiveProfiles();
        if (activeMavenProfiles.size() == 0) {
            for (Map.Entry<String, Profile> profile : mavenProfiles.entrySet()) {
                if (profile.getValue().getActivation() != null && profile.getValue().getActivation().isActiveByDefault()) {
                    activeMavenProfiles.add(profile.getKey());
                }
            }
        }
        logger.debug("Found the following active Maven profiles: " + activeMavenProfiles);
        for (String activeProfile : activeMavenProfiles) {
            Profile profile = mavenProfiles.get(activeProfile);
            List<Repository> profileRepositories = profile.getRepositories();
            for (Repository repository : profileRepositories) {
                RemoteRepository remoteRepository = new RemoteRepository.Builder(repository.getId(), "default",
                        repository.getUrl()).build();
                remoteRepositories.add(remoteRepository);
            }
        }
        if (remoteRepositories.size() == 0) {
            logger.debug("No configured remote repositories found. Using Maven central.");
            RemoteRepository mavenCentralRepo = new RemoteRepository.Builder("central", "default",
                    "http://repo1.maven.org/maven2").build();
            remoteRepositories.add(mavenCentralRepo);
        }

        return remoteRepositories;
    }

    private YggdrasilDependency resolveDependencies(@Nullable final Artifact defaultArtifact,
            @Nullable List<Dependency> mavenDependencies, final RepositorySystem repositorySystem,
            final RepositorySystemSession repositorySystemSession, final List<RemoteRepository> remoteRepositories) throws
            DependencyCollectionException, org.eclipse.aether.resolution.DependencyResolutionException
    {

        CollectRequest collectRequest = new CollectRequest();
        if (defaultArtifact != null) {
            Dependency dependency = new Dependency((defaultArtifact), null);
            collectRequest.setRoot(dependency);
        }
        if (mavenDependencies != null && mavenDependencies.size() > 0) {
            collectRequest.setDependencies(mavenDependencies);
        }

        for (RemoteRepository remoteRepository : remoteRepositories) {
            collectRequest.addRepository(remoteRepository);
        }
        DependencyNode node = repositorySystem.collectDependencies(repositorySystemSession, collectRequest).getRoot();

        if (logger.isDebugEnabled()) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(node.toString(), nodeToMap(node));
            logger.debug("Collected the following dependencies: " + logMap);
        }

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setFilter(new ScopeDependencyFilter(null));
        dependencyRequest.setRoot(node);
        repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);

        return YggdrasilDependencyMavenUtil.fromDependencyNode(node, null);
    }

    private Map<String, Object> nodeToMap(DependencyNode dependencyNode) {
        Map<String, Object> map = new HashMap<>();
        for (DependencyNode node : dependencyNode.getChildren()) {
            map.put(node.toString(), nodeToMap(node));
        }
        return map;
    }

    protected String getEnv(String key) {
        return System.getenv(key);
    }
}