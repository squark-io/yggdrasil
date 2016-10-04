package io.squark.dynamicjar.core.api;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public interface Constants {
    String DYNAMIC_JAR_GROUP_ID = "io.squark.dynamicjar";
    String DYNAMIC_JAR_CORE_ARTIFACT_ID = "dynamicjar-core";
    String DYNAMIC_JAR_API_ARTIFACT_ID = "dynamicjar-api";
    String DYNAMIC_JAR_CLASS_NAME = "io.squark.dynamicjar.core.main.DynamicJar";
    String DYNAMIC_JAR_MAVEN_PROVIDER_GROUP_ID = "io.squark.dynamicjar";
    String DYNAMIC_JAR_MAVEN_PROVIDER_ARTIFACT_ID = "dynamicjar-maven-provider";
    String DYNAMIC_JAR_LOGGING_API_GROUP_ID = "io.squark.dynamicjar.dynamicjar-logging";
    String DYNAMIC_JAR_LOGGING_FALLBACK_ARTIFACT_ID = "dynamicjar-logging-fallback";
    String DYNAMIC_JAR_BOOTSTRAP_CLASS_NAME = "io.squark.dynamicjar.core.main.Bootstrap";
    String DYNAMIC_JAR_BOOTSTRAP_GROUP_ID = "io.squark.dynamicjar";
    String DYNAMIC_JAR_BOOTSTRAP_ARTIFACT_ID = "dynamicjar-bootstrap";
    String DYNAMICJAR_RUNTIME_LIB_PATH = "META-INF/dynamicjar-runtime-lib/";
    String DYNAMICJAR_RUNTIME_OPTIONAL_LIB_PATH = "META-INF/dynamicjar-optional-lib/";
    String LIB_PATH = "META-INF/lib/";
    String YAML_PROPERTIES_FILE = "/META-INF/dynamicjar.yaml";
    String JSON_PROPERTIES_FILE = "/META-INF/dynamicjar.json";
    String DYNAMICJAR_LOG_LEVEL = "dynamicjar.logLevel";
    String NESTED_JAR_CLASSLOADER_ARTIFACT_ID = "nested-jar-classloader";
}
