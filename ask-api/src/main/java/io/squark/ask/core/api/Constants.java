package io.squark.ask.core.api;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public interface Constants {
    String ASK_GROUP_ID = "io.squark.ask";
    String ASK_CORE_ARTIFACT_ID = "ask-core";
    String ASK_API_ARTIFACT_ID = "ask-api";
    String ASK_CLASS_NAME = "io.squark.ask.core.main.Ask";
    String ASK_MAVEN_PROVIDER_GROUP_ID = "io.squark.ask";
    String ASK_MAVEN_PROVIDER_ARTIFACT_ID = "ask-maven-provider";
    String ASK_LOGGING_API_GROUP_ID = "io.squark.ask.ask-logging";
    String ASK_LOGGING_FALLBACK_ARTIFACT_ID = "ask-logging-fallback";
    String ASK_BOOTSTRAP_CLASS_NAME = "io.squark.ask.core.main.Bootstrap";
    String ASK_BOOTSTRAP_GROUP_ID = "io.squark.ask";
    String ASK_BOOTSTRAP_ARTIFACT_ID = "ask-bootstrap";
    String ASK_RUNTIME_LIB_PATH = "META-INF/ask-runtime-lib/";
    String ASK_RUNTIME_OPTIONAL_LIB_PATH = "META-INF/ask-optional-lib/";
    String LIB_PATH = "META-INF/lib/";
    String YAML_PROPERTIES_FILE = "/META-INF/ask.yaml";
    String JSON_PROPERTIES_FILE = "/META-INF/ask.json";
    String ASK_LOG_LEVEL = "ask.logLevel";
    String NESTED_JAR_CLASSLOADER_ARTIFACT_ID = "nested-jar-classloader";
}
