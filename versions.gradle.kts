allprojects {
  extra["dependencyVersions"] = mapOf(
    "kotlin" to "1.1.51",
    "weld" to "3.0.0.Final",
    "cdi-api" to "2.0",
    "servlet-api" to "3.1.0",
    "undertow" to "1.4.21.Final",
    "xnio" to "3.3.8.Final", //NOTE! Must be updated if undertow is updated
    "rs-api" to "2.0.1",
    "resteasy" to "3.1.2.Final",
    "javax.json" to "1.1.0-M1",
    "jackson-datatype-jsr353" to "2.8.7",
    "log4j" to "2.8.1",
    "gradle-bintray-plugin" to "1.7.3",
    "dokka-gradle-plugin" to "0.9.15",
    "commons-io" to "2.5",
    "maven" to "3.5.0",
    "maven-plugin-annotations" to "3.5",
    "maven-plugin-plugin" to "3.5",
    "gradle-spawn-plugin" to "0.6.0",
    "junit-platform-gradle-plugin" to "1.0.0",
    "junit-jupiter" to "5.0.0",
    "rest-assured" to "3.0.2"
  )
}
